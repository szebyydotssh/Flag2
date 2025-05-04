package eu.llamadev.flag.Menus.menu;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Managers.ArenaManager;
import eu.llamadev.flag.Menus.Menu;
import eu.llamadev.flag.Menus.MenuHandler;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ArenaSetupMenu implements Menu {

    private final MenuHandler menuHandler;
    private final Flag plugin = Flag.getPlugin(Flag.class);

    public ArenaSetupMenu(MenuHandler menuHandler) {
        this.menuHandler = menuHandler;
    }

    @Override
    public String getMenuName() {
        return "⚙ Setup Arena: ";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, getSlots(), getMenuName());

        // Example: Retrieve all arenas from a manager class
        Map<String, Arena> arenas = plugin.getArenaManager().getArenas();

        int slot = 0;
        for (Arena arena : arenas.values()) {
            if (slot >= getSlots()) break; // Prevent exceeding inventory size
            inv.setItem(slot, createItem(Material.GRASS_BLOCK, "&aArena: " + arena.getName(),
                    Arrays.asList("&7Click to configure this arena.")));
            slot++;
        }

        player.openInventory(inv);
    }


    @Override
    public void handleClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) {return; }

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (!itemName.startsWith("Arena: ")) {
            player.sendMessage("Item name does not start with 'Arena: '.");
            return;
        }

        String arenaName = itemName.replace("Arena: ", "");
        player.sendMessage("Extracted arena name: " + arenaName);

        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("Arena not found: " + arenaName);
            return;
        }

        player.sendMessage("Opening configuration menu for arena: " + arena.getName());
        menuHandler.openMenu(player, new ArenaConfigurationMenu(arena, menuHandler));

        e.setCancelled(true);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.translate(name));

        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(Color.translate(line));
            }
            meta.setLore(coloredLore);
        }

        item.setItemMeta(meta);
        return item;
    }

}
