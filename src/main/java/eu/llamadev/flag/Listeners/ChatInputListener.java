package eu.llamadev.flag.Listeners;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Team;
import eu.llamadev.flag.Menus.menu.TeamSettingsMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatInputListener implements Listener {

    private final Flag plugin = Flag.getPlugin(Flag.class);

    @EventHandler
    public void onChatInput(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (plugin.getArenaManager().waitingForMaxPlayersT.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            Team team = plugin.getArenaManager().waitingForMaxPlayersT.remove(player.getUniqueId());
            try {
                int maxPlayers = Integer.parseInt(message);
                team.getArena().setMaxPlayers(maxPlayers);
                plugin.getArenaManager().setMaxPlayersTeam(team.getArena().getName(),team.getName(), maxPlayers);
                player.sendMessage("§aMax players set to " + maxPlayers + ".");
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid number. Please try again.");
            }
            Bukkit.getScheduler().runTask(plugin, () -> new TeamSettingsMenu(team).open(player));
            return;
        }

        if (plugin.getArenaManager().waitingForMinPlayersT.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            Team team = plugin.getArenaManager().waitingForMinPlayersT.remove(player.getUniqueId());
            try {
                int minPlayers = Integer.parseInt(message);
                team.getArena().setMinPlayers(minPlayers);
                plugin.getArenaManager().setMinPlayersTeam(team.getArena().getName(), team.getName(), minPlayers);
                player.sendMessage("§aMin players set to " + minPlayers + ".");
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid number. Please try again.");
            }
            Bukkit.getScheduler().runTask(plugin, () -> new TeamSettingsMenu(team).open(player));
            return;
        }

        if (plugin.getArenaManager().waitingForNameChange.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            Team team = plugin.getArenaManager().waitingForNameChange.remove(player.getUniqueId());
            String[] parts = message.split(" ");
            if (parts.length == 2) {
                String newName = parts[0];
                try {
                    ChatColor newColor = ChatColor.valueOf(parts[1].toUpperCase());
                    plugin.getArenaManager().deleteTeam(team.getArena().getName(), team.getName());
                    plugin.getArenaManager().createTeam(team.getArena().getName(), newName, newColor.asBungee());
                    player.sendMessage("§aTeam name changed to " + newName + " and color set to " + newColor + ".");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cInvalid color. Please try again.");
                }
            } else {
                player.sendMessage("§cInvalid format. Use: <name> <color>");
            }
            Bukkit.getScheduler().runTask(plugin, () -> new TeamSettingsMenu(team).open(player));
        }
    }
}