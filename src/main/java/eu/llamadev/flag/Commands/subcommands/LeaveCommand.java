package eu.llamadev.flag.Commands.subcommands;

import eu.llamadev.flag.Commands.SubCommand;
import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.entity.Player;

public class LeaveCommand extends SubCommand {

    private final Flag plugin = Flag.getPlugin(Flag.class);

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Leave the arena";
    }

    @Override
    public String getSyntax() {
        return "/flag leave";
    }

    @Override
    public void run(Player player, String[] args) {
        Arena arena = plugin.getArenaManager().getArenaByPlayer(player);
        if (arena == null) {
            player.sendMessage(Color.translate("&cYou are not in any arena."));
            return;
        }
        plugin.getGameManager().removePlayerFromArena(player, arena);
        player.sendMessage(Color.translate("&aYou have successfully left the arena."));
    }
}