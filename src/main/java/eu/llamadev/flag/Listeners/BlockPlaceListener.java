package eu.llamadev.flag.Listeners;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Managers.Team;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

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

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.size() < 2) return;

        String arenaName = lore.get(0).replace("§7Arena: ", "");
        String teamName = lore.get(1).replace("§7Team: ", "");

        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("§cArena not found: " + arenaName);
            return;
        }

        Team team = arena.getTeamByName(teamName);
        if (team == null) {
            player.sendMessage("§cTeam not found: " + teamName);
            return;
        }

        event.setCancelled(true); // Prevent the block from being placed permanently
        Location location = event.getBlock().getLocation();
        team.setSpawnLocation(location); // Set the team's spawn location
        plugin.getArenaManager().setTeamSpawninConfig(arenaName, teamName, location);
        player.sendMessage("§aTeam spawn location set to: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ".");
    }
}