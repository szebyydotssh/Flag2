package eu.llamadev.flag.Listeners;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class TeamCreationListener implements Listener {

    private final Flag plugin = Flag.getPlugin(Flag.class);

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!plugin.getArenaManager().waitingForTeamCreation.containsKey(playerId)) return;

        e.setCancelled(true);

        String[] input = e.getMessage().split(" ");
        if (input.length < 2) {
            player.sendMessage("§cInvalid format. Use: <TeamName> <Color>");
            return;
        }

        String teamName = input[0];
        String colorName = input[1].toUpperCase();

        ChatColor color;
        try {
            color = ChatColor.valueOf(colorName);
        } catch (IllegalArgumentException ex) {
            player.sendMessage("§cInvalid color. Use a valid ChatColor name.");
            return;
        }

        Arena arena = plugin.getArenaManager().waitingForTeamCreation.remove(playerId);
        if (arena == null) {
            player.sendMessage("§cAn error occurred. Please try again.");
            return;
        }

        if (arena.teamExists(teamName)) {
            player.sendMessage("§cA team with this name already exists in the arena.");
            return;
        }

        plugin.getArenaManager().createTeam(arena.getName(), teamName, color);
        player.sendMessage("§aTeam " + teamName + " with color " + color + " created successfully!");
    }
}