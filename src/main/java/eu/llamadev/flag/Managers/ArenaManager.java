package eu.llamadev.flag.Managers;

import eu.llamadev.flag.Flag;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class ArenaManager {

    private final Map<String, Arena> arenas = new HashMap<>();
    private final Flag plugin = Flag.getPlugin(Flag.class);
    public static Map<UUID, Arena> waitingForMaxPlayers = new HashMap<>();
    public static Map<UUID, Arena> waitingForMinPlayers = new HashMap<>();
    public final Map<UUID, Team> waitingForMaxPlayersT = new HashMap<>();
    public final Map<UUID, Team> waitingForMinPlayersT = new HashMap<>();
    public final Map<UUID, Team> waitingForNameChange = new HashMap<>();
    public final Map<UUID, Arena> waitingForTeamCreation = new HashMap<>();
    public final Map<UUID, Arena> waitingForTeamSpawn = new HashMap<>();


    public void createArena(Arena arena) {
        arenas.put(arena.getName(), arena);
        File file = new File(plugin.getDataFolder(), "arenas/" + arena.getName() + ".yml");

        if (file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("name", arena.getName());
        config.set("maxPlayers", arena.getMaxPlayers());
        config.set("minPlayers", arena.getMinPlayers());
        config.set("pos1.world", arena.getPos1().getWorld().getName());
        config.set("pos1.x", arena.getPos1().getX());
        config.set("pos1.y", arena.getPos1().getY());
        config.set("pos1.z", arena.getPos1().getZ());
        config.set("pos2.world", arena.getPos2().getWorld().getName());
        config.set("pos2.x", arena.getPos2().getX());
        config.set("pos2.y", arena.getPos2().getY());
        config.set("pos2.z", arena.getPos2().getZ());
        config.set("lobbyLocation.world", arena.getLobbyLocation().getWorld().getName());
        config.set("lobbyLocation.x", arena.getLobbyLocation().getX());
        config.set("lobbyLocation.y", arena.getLobbyLocation().getY());
        config.set("lobbyLocation.z", arena.getLobbyLocation().getZ());
        config.set("lobbyLocation.yaw", arena.getLobbyLocation().getYaw());
        config.set("lobbyLocation.pitch", arena.getLobbyLocation().getPitch());
        config.set("spectatorLocation.world", arena.getLobbyLocation().getWorld().getName());
        config.set("spectatorLocation.x", arena.getSpectatorLocation().getX());
        config.set("spectatorLocation.y", arena.getSpectatorLocation().getY());
        config.set("spectatorLocation.z", arena.getSpectatorLocation().getZ());
        config.set("spectatorLocation.yaw", arena.getSpectatorLocation().getYaw());
        config.set("spectatorLocation.pitch", arena.getSpectatorLocation().getPitch());

        for (Team team : arena.getTeams()) {
            String path = "teams." + team.getName();
            config.set(path + ".color", team.getColor());
            setLocation(config, path + ".spawn", team.getSpawnLocation());
        }
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadArenas() {
        File arenasFolder = new File(plugin.getDataFolder(), "arenas");

        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
        }

        File[] files = arenasFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            Bukkit.getLogger().info("No arenas found to load.");
            return;
        }

        for (File file : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String name = config.getString("name");
            int maxPlayers = config.getInt("maxPlayers");
            int minPlayers = config.getInt("minPlayers");

            // Load arena positions
            Location pos1 = getLocation(config, "pos1");
            Location pos2 = getLocation(config, "pos2");

            // Load lobby and spectator locations
            Location lobbyLocation = getLocation(config, "lobbyLocation");
            Location spectatorLocation = getLocation(config, "spectatorLocation");

            if (pos1 == null || pos2 == null || lobbyLocation == null || spectatorLocation == null) {
                Bukkit.getLogger().warning("[FLAG] Failed to load arena '" + name + "': Missing required locations.");
                continue;
            }

            Arena arena = new Arena(name, pos1, pos2, spectatorLocation, lobbyLocation, maxPlayers, minPlayers);

            ConfigurationSection teamsSection = config.getConfigurationSection("teams");
            if (teamsSection != null) {
                for (String teamName : teamsSection.getKeys(false)) {
                    ConfigurationSection teamSection = teamsSection.getConfigurationSection(teamName);
                    if (teamSection == null) continue;

                    String colorString = teamSection.getString("color");
                    int teamMaxPlayers = teamSection.getInt("maxPlayers"); // No default value
                    int teamMinPlayers = teamSection.getInt("minPlayers"); // No default value
                    Location spawn = getLocation(config, "teams." + teamName + ".spawn");
                    Location flagLocation = getLocation(config, "teams." + teamName + ".flag");

                    try {
                        ChatColor color = ChatColor.valueOf(colorString.toUpperCase());
                        Team team = new Team(teamName, color, teamMaxPlayers, teamMinPlayers, spawn, arena);
                        arena.addTeam(team);
                        team.setTeamMinPlayers(teamMinPlayers);
                        team.setFlagLocation(flagLocation);
                        team.setTeamMaxPlayers(teamMaxPlayers);
                    } catch (IllegalArgumentException e) {
                        Bukkit.getLogger().warning("[FLAG] Failed to load team '" + teamName + "' in arena '" + name + "': Invalid color '" + colorString + "'");
                    }
                }
            }

            arenas.put(name, arena);
            Bukkit.getLogger().info("[FLAG] Successfully loaded arena: " + name);


        }
        debugFlagLocations();
    }
    public void debugFlagLocations() {
        for (Arena arena : arenas.values()) {
            Bukkit.getLogger().info("Debugging flag locations for arena: " + arena.getName());
            for (Team team : arena.getTeams()) {
                Location flagLocation = team.getFlagLocation();
                if (flagLocation != null) {
                    Bukkit.getLogger().info("Team: " + team.getName() + ", Flag Location: " +
                            "World: " + flagLocation.getWorld().getName() +
                            ", X: " + flagLocation.getX() +
                            ", Y: " + flagLocation.getY() +
                            ", Z: " + flagLocation.getZ());
                } else {
                    Bukkit.getLogger().warning("Team: " + team.getName() + " has no flag location set.");
                }
            }
        }
    }
    private Location getLocation(FileConfiguration config, String path) {
        if (!config.contains(path)) return null;

        String worldName = config.getString(path + ".world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public Arena getArena(String name) {
        return this.arenas.get(name);
    }
    public Arena getArenaByPlayer(Player player) {
        for (Arena arena : arenas.values()) {
            if (arena.getPlayers().contains(player.getName())) {
                return arena;
            }
        }
        return null;
    }

    public boolean arenaExists(String name) {
        return this.arenas.containsKey(name);
    }

    public Map<String, Arena> getArenas() {
        return this.arenas;
    }

    private void setLocation(FileConfiguration config, String path, Location loc) {
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
    }

    private Location getLocation(FileConfiguration config, String path, World world) {
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Location standardLocation() {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        float yaw = (float) 0;
        float pitch = (float) 0;
        World world = Bukkit.getWorld("world");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setArenaPos1(String arenaName, Location pos1) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        arena.setPos1(pos1);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("pos1.world", pos1.getWorld().getName());
        config.set("pos1.x", pos1.getX());
        config.set("pos1.y", pos1.getY());
        config.set("pos1.z", pos1.getZ());

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setArenaPos2(String arenaName, Location pos2) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        arena.setPos2(pos2);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("pos2.world", pos2.getWorld().getName());
        config.set("pos2.x", pos2.getX());
        config.set("pos2.y", pos2.getY());
        config.set("pos2.z", pos2.getZ());

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSpectatorSpawn(String arenaName, Location location) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        arena.setSpectatorSpawn(location);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("spectatorLocation.world", location.getWorld().getName());
        config.set("spectatorLocation.x", location.getX());
        config.set("spectatorLocation.y", location.getY());
        config.set("spectatorLocation.z", location.getZ());

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLobbyLocation(String arenaName, Location location) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        arena.setLobbyLocation(location);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("lobbyLocation.world", location.getWorld().getName());
        config.set("lobbyLocation.x", location.getX());
        config.set("lobbyLocation.y", location.getY());
        config.set("lobbyLocation.z", location.getZ());

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMinPlayers(String arenaName, int minPlayers) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        arena.setMinPlayers(minPlayers);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("minPlayers", minPlayers);

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setMaxPlayers(String arenaName, int maxPlayers) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        arena.setMaxPlayers(maxPlayers);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("maxPlayers", maxPlayers);

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setMinPlayersTeam(String arenaName, String teamName, int minPlayers) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        Team team = arena.getTeamByName(teamName);
        if (team == null) {
            Bukkit.getLogger().warning("[FLAG] Team not found: " + teamName);
            return;
        }

        team.setTeamMinPlayers(minPlayers);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String path = "teams." + teamName + ".minPlayers";
        config.set(path, minPlayers);

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setMaxPlayersTeam(String arenaName, String teamName, int maxPlayers) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        Team team = arena.getTeamByName(teamName);
        if (team == null) {
            Bukkit.getLogger().warning("[FLAG] Team not found: " + teamName);
            return;
        }

        team.setTeamMaxPlayers(maxPlayers);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String path = "teams." + teamName + ".maxPlayers";
        config.set(path, maxPlayers);

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void createTeam(String arenaName, String teamName, ChatColor color) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        Team team = new Team(teamName, color, 4, 1, null, arena);
        arena.addTeam(team);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String path = "teams." + teamName;
        config.set(path + ".color", color.name());
        config.set(path + ".maxPlayers", team.getTeamMaxPlayers());
        config.set(path + ".minPlayers", team.getTeamMinPlayers());
        if (team.getSpawnLocation() != null) {
            setLocation(config, path + ".spawn", team.getSpawnLocation());
        }

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().info("[FLAG] Team " + teamName + " created for arena: " + arenaName);
    }

    public void deleteTeam(String arenaName, String teamName) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        Team team = arena.getTeamByName(teamName);
        if (team == null) {
            Bukkit.getLogger().warning("[FLAG] Team not found: " + teamName);
            return;
        }

        arena.removeTeam(team);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("teams." + teamName, null);

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().info("[FLAG] Team " + teamName + " deleted from arena: " + arenaName);
    }

    public void setTeamSpawn(Player player, Team team) {
        ItemStack block = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = block.getItemMeta();
        meta.setDisplayName("§6Team Spawn Setter");
        meta.setLore(List.of(
                "§7Arena: " + team.getArena().getName(),
                "§7Team: " + team.getName(),
                "§7Color: " + team.getColor().name(),
                "§eLeft-Click to place"
        ));
        block.setItemMeta(meta);

        player.getInventory().addItem(block);
        player.sendMessage("§aPlace the gold block to set the team's spawn location.");
        waitingForTeamSpawn.put(player.getUniqueId(), team.getArena());
    }
    public void setTeamSpawninConfig(String arenaName, String teamName, Location location) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            Bukkit.getLogger().warning("[FLAG] Arena not found: " + arenaName);
            return;
        }

        Team team = arena.getTeamByName(teamName);
        if (team == null) {
            Bukkit.getLogger().warning("[FLAG] Team not found: " + teamName);
            return;
        }

        team.setSpawnLocation(location);

        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[FLAG] Arena config file not found for: " + arenaName);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String path = "teams." + teamName + ".spawn";
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void teleportPlayerToArenaLobby(Player player, Arena arena) {
        if (arena == null) {
            player.sendMessage("§cArena not found.");
            return;
        }
        Location lobbyLocation = arena.getLobbyLocation();
        if (lobbyLocation == null) {
            player.sendMessage("§cLobby location not set for this arena.");
            return;
        }
        player.teleport(lobbyLocation);
        player.sendMessage("§aTeleported to arena lobby: " + arena.getName());
    }
    public void saveLuckyBlockLocation(Arena arena, Location location) {
        File file = new File(plugin.getDataFolder(), "arenas/" + arena.getName() + ".yml");
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> luckyBlocks = config.getStringList("luckyBlocks");
        luckyBlocks.add(location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ());
        config.set("luckyBlocks", luckyBlocks);

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getLuckyBlockLocations(Arena arena) {
        File file = new File(plugin.getDataFolder(), "arenas/" + arena.getName() + ".yml");
        if (!file.exists()) return new ArrayList<>();

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getStringList("luckyBlocks");
    }

    public void saveLuckyBlockLocations(Arena arena, List<String> luckyBlocks) {
        File file = new File(plugin.getDataFolder(), "arenas/" + arena.getName() + ".yml");
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("luckyBlocks", luckyBlocks);

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setFlagLocationInConfig(String arenaName, String teamName, Location location) {
        File file = new File(plugin.getDataFolder(), "arenas/" + arenaName + ".yml");
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String path = "teams." + teamName + ".flag";

        if (location == null) {
            config.set(path, null);
        } else {
            config.set(path + ".world", location.getWorld().getName());
            config.set(path + ".x", location.getX());
            config.set(path + ".y", location.getY());
            config.set(path + ".z", location.getZ());
        }

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
