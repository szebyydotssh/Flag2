package eu.llamadev.flag.Commands;

import eu.llamadev.flag.Commands.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CommandManager implements CommandExecutor {

    private ArrayList<SubCommand> subcommands = new ArrayList<>();

    public CommandManager() {
        subcommands.add(new CreateArena());
        subcommands.add(new SetupCommand());
        subcommands.add(new CreateTeam());
        subcommands.add(new JoinArena());
        subcommands.add(new LeaveCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(args.length > 0) {
                for (int i = 0; i < getSubCommands().size(); i++) {
                    if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                        getSubCommands().get(i).run(p, args);
                    }
                }
            }else if(args.length == 0) {
                    p.sendMessage("");
                    for(int i = 0; i < getSubCommands().size();i++) {
                        p.sendMessage(getSubCommands().get(i).getSyntax() + " - " + getSubCommands().get(i).getDescription());
                    }
               p.sendMessage("");
                }
            }
        return true;
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subcommands;
    }
}
