package eu.llamadev.flag.Commands.subcommands;

import eu.llamadev.flag.Commands.SubCommand;
import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Menus.Menu;
import eu.llamadev.flag.Menus.menu.ArenaSetupMenu;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SetupCommand extends SubCommand {

    private final Flag plugin = Flag.getPlugin(Flag.class);
    @Override
    public String getName() {
        return "setup";
    }

    @Override
    public String getDescription() {
        return "Setup menu";
    }

    @Override
    public String getSyntax() {
        return "/flag setup";
    }

    @Override
    public void run(Player player, String[] args) {
        Menu menu = new ArenaSetupMenu(plugin.getMenuHandler());
        plugin.getMenuHandler().openMenu(player, menu);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 4, 4);
        player.sendMessage(Color.translate("&fFLAG &7» &aOpening setup menu..."));
    }
}
