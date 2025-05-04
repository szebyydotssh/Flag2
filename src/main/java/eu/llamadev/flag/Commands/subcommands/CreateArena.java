package eu.llamadev.flag.Commands.subcommands;

import eu.llamadev.flag.Commands.SubCommand;
import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CreateArena extends SubCommand {

    private final Flag plugin = Flag.getPlugin(Flag.class);
    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Creates an arena";
    }

    @Override
    public String getSyntax() {
        return "/flag create <name>";
    }

    @Override
    public void run(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(Color.translate("&cUsage: /luckyflag create arena <name>"));
            return;
        }
        String name = args[1];
        if (plugin.getArenaManager().getArena(name) != null) {
            player.sendMessage(Color.translate("&cAn arena with that name already exists."));
            return;
        }
        Arena arena = new Arena(args[1].toString(), plugin.getArenaManager().standardLocation(),  plugin.getArenaManager().standardLocation(),  plugin.getArenaManager().standardLocation(),  plugin.getArenaManager().standardLocation(), 16, 4);
        plugin.getArenaManager().createArena(arena);
        player.sendMessage(Color.translate("Arena created"));
    }
}
