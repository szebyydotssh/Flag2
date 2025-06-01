package eu.llamadev.flag.Commands.subcommands;

import eu.llamadev.flag.Commands.SubCommand;
import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.entity.Player;

public class JoinArena extends SubCommand {

    private final Flag plugin = Flag.getPlugin(Flag.class);

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Join an arena";
    }

    @Override
    public String getSyntax() {
        return "/flag join <arena>";
    }

    @Override
    public void run(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Color.translate("&cUsage: /flag join <arena>"));
            return;
        }
        String name = args[1];
        if (plugin.getArenaManager().getArena(name) == null) {
            player.sendMessage(Color.translate("&cArena with this name not exists."));
            return;
        }
        plugin.getGameManager().addPlayerToArena(player, plugin.getArenaManager().getArena(name));
        plugin.getGameManager().setLobbyScoreboard(plugin.getArenaManager().getArena(name));
        plugin.getArenaManager().teleportPlayerToArenaLobby(player, plugin.getArenaManager().getArena(name));

    }
}
