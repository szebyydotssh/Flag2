package eu.llamadev.flag.Listeners;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PositionToolListener implements Listener {

    private final Flag plugin = Flag.getPlugin(Flag.class);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.BLAZE_ROD || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
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
            player.sendMessage("No arena found in the tool's lore.");
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("Arena not found: " + arenaName);
            return;
        }

        Location playerLocation = player.getLocation();

        switch (e.getAction()) {
            case RIGHT_CLICK_BLOCK:
                plugin.getArenaManager().setArenaPos1(arena.getName(), e.getClickedBlock().getLocation());
                player.sendMessage("Pos1 set to block location for arena: " + arenaName);
                break;
            case LEFT_CLICK_BLOCK:
                plugin.getArenaManager().setArenaPos2(arena.getName(), e.getClickedBlock().getLocation());
                player.sendMessage("Pos2 set to block location for arena: " + arenaName);
                break;
            case RIGHT_CLICK_AIR:
                plugin.getArenaManager().setArenaPos1(arena.getName(), playerLocation);
                player.sendMessage("Pos1 set to your current location for arena: " + arenaName);
                break;
            case LEFT_CLICK_AIR:
                plugin.getArenaManager().setArenaPos2(arena.getName(), playerLocation);
                player.sendMessage("Pos2 set to your current location for arena: " + arenaName);
                break;
            default:
                break;
        }
        e.setCancelled(true);
    }
    public Block getTargetBlock(Player player, int maxDistance) {
        Block target = player.getTargetBlockExact(maxDistance);
        return target;
    }
}