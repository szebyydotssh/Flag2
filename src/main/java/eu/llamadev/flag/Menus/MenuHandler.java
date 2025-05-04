package eu.llamadev.flag.Menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class MenuHandler implements Listener {

    private final JavaPlugin plugin;
    private final HashMap<UUID, Menu> openMenus = new HashMap<>();

    public MenuHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player, Menu menu) {
        openMenus.put(player.getUniqueId(), menu);
        menu.open(player);
    }

    @EventHandler
    public void handle(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        Menu menu = openMenus.get(player.getUniqueId());
        if (menu != null && (
                e.getView().getTitle().startsWith("⚙ Setup Arena:") ||
                        e.getView().getTitle().equals(menu.getMenuName())
        )) {
            e.setCancelled(true);
            menu.handleClick(e);
        }
    }
}