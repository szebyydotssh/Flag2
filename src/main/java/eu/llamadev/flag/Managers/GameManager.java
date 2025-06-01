package eu.llamadev.flag.Managers;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Utils.Color;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameManager {

    private final Flag plugin;
    private GameState gameState;
    private int countdownTaskId = -1;
    private final Map<Player, Team> flagCarriers = new HashMap<>();


    public GameManager(Flag plugin) {
        this.plugin = plugin;
        this.gameState = GameState.LOBBY;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Flag getPlugin() {
        return plugin;
    }

    public void addPlayerToArena(Player player, Arena arena) {
        if (arena.getPlayers().contains(player.getName())) {
            player.sendMessage("You are already in the arena.");
            return;
        }

        arena.addPlayer(player.getName());
        player.sendMessage("You have joined the arena: " + arena.getName());
        updateTablistForArena(arena);

        if (arena.getPlayers().size() >= arena.getMinPlayers() && getGameState() == GameState.LOBBY) {
            startCountdown(arena);
        }
    }
    public void removePlayerFromArena(Player player, Arena arena) {
        if (!arena.getPlayers().contains(player.getName())) {
            player.sendMessage("You are not in the arena.");
            return;
        }

        arena.removePlayer(player.getName());
        restoreTablistForPlayer(player);
        player.sendMessage("You have left the arena.");

        if (arena.getPlayers().size() < arena.getMinPlayers() && countdownTaskId != -1) {
            Bukkit.getScheduler().cancelTask(countdownTaskId);
            countdownTaskId = -1;
            Bukkit.broadcastMessage("Countdown canceled due to insufficient players.");
        }
    }
    private void startCountdown(Arena arena) {
        if (getGameState() != GameState.LOBBY) {
            return;
        }
        ArenaBlockManager blockManager = new ArenaBlockManager(plugin.getDataFolder());
        blockManager.restoreArenaBlocks(arena);
        setGameState(GameState.STARTING);
        Bukkit.broadcastMessage("Minimum players reached! Starting game in 10 seconds...");

        countdownTaskId = new BukkitRunnable() {
            int countdown = 10;

            @Override
            public void run() {
                if (arena.getPlayers().size() < arena.getMinPlayers()) {
                    Bukkit.broadcastMessage("Countdown canceled due to insufficient players.");
                    setGameState(GameState.LOBBY);
                    countdownTaskId = -1;
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    if (getGameState() == GameState.INGAME) {
                        cancel();
                        return;
                    }

                    setGameState(GameState.INGAME);
                    Bukkit.broadcastMessage("Game started!");
                    startArenaTimer(arena);
                    assignPlayersToTeams(arena);
                    teleportPlayersToTeamSpawns(arena);
                    setArenaScoreboard(arena);
                    updateTablistForArena(arena);
                    countdownTaskId = -1;
                    cancel();
                    return;
                }

                Bukkit.broadcastMessage("Game starting in " + countdown + " seconds...");
                countdown--;
            }
        }.runTaskTimer(plugin, 0, 20).getTaskId();
    }

    public void assignPlayersToTeams(Arena arena) {
        List<Team> teams = new ArrayList<>(arena.getTeams());
        List<String> players = new ArrayList<>(arena.getPlayers());

        Collections.shuffle(teams);

        for (String playerName : players) {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) continue;

            if (arena.getTeamByPlayer(playerName) != null) continue;

            for (Team team : teams) {
                if (team.getPlayers().size() < team.getTeamMaxPlayers()) {
                    team.addPlayer(playerName);
                    player.sendMessage("You have been assigned to team: " + team.getName());
                    break;
                }
            }
        }
    }
    public void teleportPlayersToTeamSpawns(Arena arena) {
        for (Team team : arena.getTeams()) {
            for (String playerName : team.getPlayers()) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    player.teleport(team.getSpawnLocation());
                    player.sendMessage("You have been teleported to your team's spawn.");
                }
            }
        }
    }
    public void setLobbyScoreboard(Arena arena) {
        FileConfiguration config = plugin.getConfig();

        String title = config.getString("lobby-scoreboard.title", "&l&6FLAG");
        List<String> scores = config.getStringList("lobby-scoreboard.scores");
        List<String> players = new ArrayList<>(arena.getPlayers());

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard scoreboard = manager.getNewScoreboard();
        org.bukkit.scoreboard.Objective objective = scoreboard.registerNewObjective("lobby", "dummy", Color.translate(title));
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        int scoreValue = scores.size();
        int emptyLineCounter = 0;
        for (String line : scores) {
            if (line.isEmpty()) {
                line = "§r" + " ".repeat(emptyLineCounter++);
            }
            String translatedLine = Color.translate(line)
                    .replace("%status%", getGameState().toString())
                    .replace("%players%", String.valueOf(arena.getPlayers().size()))
                    .replace("%arena%", arena.getName())
                    .replace("%match_id%", "12345");
            objective.getScore(translatedLine).setScore(scoreValue--);
        }

        for (String playerName : players) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                player.setScoreboard(scoreboard);
            }
        }
    }
    public void setArenaScoreboard(Arena arena) {
        FileConfiguration config = plugin.getConfig();

        String title = config.getString("arena-scoreboard.title", "&l&6FLAG");
        List<String> scores = config.getStringList("arena-scoreboard.scores");
        List<String> players = new ArrayList<>(arena.getPlayers());

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard scoreboard = manager.getNewScoreboard();
        org.bukkit.scoreboard.Objective objective = scoreboard.registerNewObjective("arena", "dummy", Color.translate(title));
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        int scoreValue = scores.size();
        int emptyLineCounter = 0;

        for (String line : scores) {
            if (line.isEmpty()) {
                line = "§r" + " ".repeat(emptyLineCounter++);
            } else if (line.equals("%list_teams%")) {
                for (Team team : arena.getTeams()) {
                    String flagSymbol = getFlagSymbol(team.getFlagStatus());
                    String teamLine = flagSymbol + " " + team.getColor() + team.getName() + "§r: " + team.getPlayers().size();
                    objective.getScore(teamLine).setScore(scoreValue--);
                }
                continue;
            }
            String translatedLine = Color.translate(line)
                    .replace("%time%", formatTime(arena.getRemainingTime()))
                    .replace("%arena%", arena.getName())
                    .replace("%match_id%", "12345");
            objective.getScore(translatedLine).setScore(scoreValue--);
        }

        for (String playerName : players) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                player.setScoreboard(scoreboard);
            }
        }
    }
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
    public void startArenaTimer(Arena arena) {
        arena.setRemainingTime(3600);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (arena.getRemainingTime() <= 0) {
                    endArena(arena);
                    cancel();
                    return;
                }

                arena.setRemainingTime(arena.getRemainingTime() - 1);
                updateArenaScoreboard(arena);
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void updateArenaScoreboard(Arena arena) {
        setArenaScoreboard(arena);
    }

    private void endArena(Arena arena) {
        setGameState(GameState.ENDING);
        Bukkit.broadcastMessage("The game in arena " + arena.getName() + " has ended!");
    }
    private String getFlagSymbol(FlagStatus status) {
        switch (status) {
            case ALIVE:
                return "§a🏴";
            case STOLEN:
                return "§e⚠️";
            case BROKEN:
                return "§cBR"; // Ensure "BROKEN" status remains unchanged
            default:
                return "§7?";
        }
    }
    public void updateTablistForArena(Arena arena) {
        List<String> players = new ArrayList<>(arena.getPlayers());
        for (String playerName : players) {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) continue;

            Team team = arena.getTeamByPlayer(playerName);
            if (team != null) {
                player.setPlayerListName(team.getColor() + player.getName());
            }
        }
    }

    public void restoreTablistForPlayer(Player player) {
        player.setPlayerListName(player.getName());
    }
    public void trackFlagCarrier(Player player, Team team) {
        team.setFlagStatus(FlagStatus.STOLEN);
        player.sendMessage("§cYou have stolen the flag of team: " + team.getName() + "!");

        // Spawn an armor stand above the player's head to display the flag
        Location flagLocation = player.getLocation().add(0, 2, 0);
        ArmorStand flagStand = flagLocation.getWorld().spawn(flagLocation, ArmorStand.class);
        flagStand.setInvisible(true);
        flagStand.setGravity(false);
        flagStand.setHelmet(new ItemStack(Material.WHITE_BANNER)); // Replace with your flag item
        flagStand.addPassenger(player);

        // Update the scoreboard
        Arena arena = team.getArena();
        if (arena != null) {
            setArenaScoreboard(arena);
        }
    }

    public void handleFlagReturn(Player player, Team team) {
        if (team.getFlagStatus() == FlagStatus.STOLEN) {
            team.setFlagStatus(FlagStatus.ALIVE);
            player.sendMessage("§aYou have returned the flag to your team!");
            removeFlagCarrier(player);

            // Update the scoreboard
            Arena arena = team.getArena();
            if (arena != null) {
                setArenaScoreboard(arena);
            }
        }
    }

    public void addFlagCarrier(Player player, Team team) {
        flagCarriers.put(player, team);
        team.setFlagStatus(FlagStatus.STOLEN);

        // Spawn an armor stand above the player's head to display the flag
        Location flagLocation = player.getLocation().add(0, 2, 0);
        ArmorStand flagStand = flagLocation.getWorld().spawn(flagLocation, ArmorStand.class);
        flagStand.setInvisible(true);
        flagStand.setGravity(false);
        flagStand.setHelmet(new ItemStack(Material.WHITE_BANNER)); // Replace with your flag item
        flagStand.addPassenger(player);

        // Update the scoreboard
        setArenaScoreboard(team.getArena());
    }
    public void removeFlagCarrier(Player player) {
        Team team = flagCarriers.remove(player);
        if (team != null) {
            team.setFlagStatus(FlagStatus.ALIVE);
        }

        // Remove the flag entity above the player's head
        player.getPassengers().forEach(entity -> {
            if (entity instanceof ArmorStand) {
                entity.remove();
            }
        });
    }
    public Team getFlagCarrierTeam(Player player) {
        return flagCarriers.get(player);
    }
    public boolean isFlagCarrier(Player player) {
        return flagCarriers.containsKey(player);
    }

    public ItemMeta createBannerMeta(ChatColor color) {
        ItemStack banner = new ItemStack(Material.WHITE_BANNER);
        ItemMeta meta = banner.getItemMeta();
        meta.setDisplayName(color + "Team Flag");
        return meta;
    }
}