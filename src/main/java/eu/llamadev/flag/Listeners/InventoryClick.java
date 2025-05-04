package eu.llamadev.flag.Listeners;

import eu.llamadev.flag.Flag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClick implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Flag.getPlugin(Flag.class).getMenuHandler().handle(e);
    }

}
