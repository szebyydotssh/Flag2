package eu.llamadev.flag.Commands.subcommands;

import eu.llamadev.flag.Commands.SubCommand;
import eu.llamadev.flag.Flag;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class CreateTeam extends SubCommand {

    private final Flag plugin = Flag.getPlugin(Flag.class);

    @Override
    public String getName() {
        return "createteam";
    }

    @Override
    public String getDescription() {
        return "Creates a new team";
    }

    @Override
    public String getSyntax() {
        return "/flag createteam <arena> <name>";
    }

    @Override
    public void run(Player player, String[] args) {
        if(args.length < 3) {
            player.sendMessage("§cUsage: " + getSyntax());
            return;
        }
        String arenaName = args[1];
        String teamName = args[2];

        if (!plugin.getArenaManager().arenaExists(arenaName)) {
            player.sendMessage("§cArena " + arenaName + " does not exist.");
            return;
        }
        if (plugin.getArenaManager().getArena(arenaName).teamExists(teamName)) {
            player.sendMessage("§cTeam " + teamName + " already exists.");
            return;
        }
        plugin.getArenaManager().createTeam(arenaName, teamName, ChatColor.WHITE);
        player.sendMessage("§aTeam " + teamName + " created in arena " + arenaName + ".");
    }
}
