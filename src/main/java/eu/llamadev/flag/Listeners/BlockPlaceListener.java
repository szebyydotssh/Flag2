package eu.llamadev.flag.Listeners;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Managers.Team;
import eu.llamadev.flag.Menus.menu.TeamSettingsMenu;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;

public class BlockPlaceListener implements Listener {

    private final Flag plugin = Flag.getPlugin(Flag.class);

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItemInHand();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return;
        }

        List<String> lore = item.getItemMeta().getLore();
        String arenaName = null;

        for (String line : lore) {
            if (line.startsWith("§7Selected Arena: ")) {
                arenaName = Color.strip(line.replace("Selected Arena: ", "").trim());
                break;
            }
        }

        if (arenaName == null) {
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("Arena not found: " + arenaName);
            return;
        }

        Location blockLocation = e.getBlock().getLocation();

        if (item.getType() == Material.EMERALD_BLOCK) {
            plugin.getArenaManager().setSpectatorSpawn(arena.getName(), blockLocation);
            player.sendMessage("Spectator spawn set for arena: " + arenaName);
        } else if (item.getType() == Material.GOLD_BLOCK) {
            plugin.getArenaManager().setLobbyLocation(arena.getName(), blockLocation);
            player.sendMessage("Lobby location set for arena: " + arenaName);
        }

        e.getBlock().setType(Material.AIR);
        e.setCancelled(true);
    }
    @EventHandler
    public void onBlockPlaceF(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return;

        if (item.getType() != Material.GOLD_BLOCK) return;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.size() < 2) return;

        String arenaName = lore.get(0).replace("§7Arena: ", "");
        String teamName = lore.get(1).replace("§7Team: ", "");

        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) return;

        Team team = arena.getTeamByName(teamName);
        if (team == null) return;

        event.setCancelled(true);
        Location location = event.getBlock().getLocation();
        team.setSpawnLocation(location);
        plugin.getArenaManager().setTeamSpawninConfig(arenaName, teamName, location);

        player.sendMessage("§aTeam spawn location set to: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ".");
        player.setItemInHand(new ItemStack(Material.AIR));

        // Check if both flag and spawn locations are set
        if (team.getSpawnLocation() != null && team.getFlagLocation() != null) {
            plugin.getMenuHandler().openMenu(player, new TeamSettingsMenu(team));
        }
    }
    @EventHandler
    public void onLuckyBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getDisplayName().equals(Color.translate("&6Place LuckyBlock"))) return;

        if (item.getType() != Material.YELLOW_STAINED_GLASS) return;

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            player.sendMessage("§cInvalid LuckyBlock item lore.");
            return;
        }

        String arenaName = Color.strip(lore.get(0).replace("Arena: ", "").trim());

        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("§cArena not found: " + arenaName);
            return;
        }

        Location blockLocation = event.getBlock().getLocation();
        plugin.getArenaManager().saveLuckyBlockLocation(arena, blockLocation);

        player.sendMessage("§aLuckyBlock added to arena: " + arenaName + ".");
    }
    @EventHandler
    public void onFlagPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getDisplayName().equals(Color.translate("&6Place Flag"))) return;

        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 2) return;

        String arenaName = Color.strip(lore.get(0).replace("Arena: ", ""));
        String teamName = Color.strip(lore.get(1).replace("Team: ", ""));

        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) return;

        Team team = arena.getTeamByName(teamName);
        if (team == null) return;

        Location blockLocation = event.getBlock().getLocation();
        saveFlagLocation(arena, team, blockLocation);

        player.sendMessage("§aFlag location set for team: " + teamName);
        player.setItemInHand(new ItemStack(Material.AIR));

        // Check if both flag and spawn locations are set
        if (team.getSpawnLocation() != null && team.getFlagLocation() != null) {
            plugin.getMenuHandler().openMenu(player, new TeamSettingsMenu(team));
        }
    }

    private void saveFlagLocation(Arena arena, Team team, Location location) {
        File file = new File(plugin.getDataFolder(), "arenas/" + arena.getName() + ".yml");
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String path = "teams." + team.getName() + ".flag";
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}