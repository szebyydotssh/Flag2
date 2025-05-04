package eu.llamadev.flag.Listeners;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Managers.ArenaManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class PlayerChatListener implements Listener {

    private final Flag plugin = Flag.getPlugin(Flag.class);

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        System.out.println("Chat event triggered by: " + event.getPlayer().getName());

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (ArenaManager.waitingForMaxPlayers.containsKey(playerId)) {
            event.setCancelled(true);

            String message = event.getMessage();
            try {
                int maxPlayers = Integer.parseInt(message);
                if (maxPlayers <= 0) {
                    player.sendMessage(ChatColor.RED + "The number must be greater than 0.");
                    return;
                }

                Arena arena = ArenaManager.waitingForMaxPlayers.remove(playerId);
                arena.setMaxPlayers(maxPlayers);
                plugin.getArenaManager().setMaxPlayers(arena.getName(), maxPlayers);
                player.sendMessage(ChatColor.GREEN + "Max players set to " + maxPlayers + " for arena " + arena.getName() + ".");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter a valid number.");
            }
        } else if (ArenaManager.waitingForMinPlayers.containsKey(playerId)) {
            event.setCancelled(true);

            String message = event.getMessage();
            try {
                int minPlayers = Integer.parseInt(message);
                if (minPlayers <= 0) {
                    player.sendMessage(ChatColor.RED + "The number must be greater than 0.");
                    return;
                }

                Arena arena = ArenaManager.waitingForMinPlayers.remove(playerId);
                arena.setMinPlayers(minPlayers);
                plugin.getArenaManager().setMinPlayers(arena.getName(), minPlayers);
                player.sendMessage(ChatColor.GREEN + "Min players set to " + minPlayers + " for arena " + arena.getName() + ".");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter a valid number.");
            }
        }
    }
}