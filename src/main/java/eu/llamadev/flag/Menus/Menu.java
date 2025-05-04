package eu.llamadev.flag.Menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface Menu {
    String getMenuName();
    int getSlots();
    void handleClick(InventoryClickEvent e);
    void open(Player player);
}
