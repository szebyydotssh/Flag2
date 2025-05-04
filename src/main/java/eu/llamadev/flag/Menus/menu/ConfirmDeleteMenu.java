package eu.llamadev.flag.Menus.menu;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Menus.Menu;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfirmDeleteMenu implements Menu {

    private final Arena arena;
    private final Flag plugin = Flag.getPlugin(Flag.class);

    public ConfirmDeleteMenu(Arena arena) {
        this.arena = arena;
    }

    @Override
    public String getMenuName() {
        return "Confirm Deletion: " + arena.getName();
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, getSlots(), getMenuName());

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < getSlots(); i++) {
            inv.setItem(i, filler);
        }

        inv.setItem(11, createItem(Material.EMERALD_BLOCK, "&aConfirm", null));
        inv.setItem(15, createItem(Material.BARRIER, "&cCancel", null));

        player.openInventory(inv);
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        String itemName = Color.strip(clickedItem.getItemMeta().getDisplayName());

        switch (itemName) {
            case "Confirm":
                File arenaFile = new File(plugin.getDataFolder(), "arenas/" + arena.getName() + ".yml");
                if (arenaFile.exists()) {
                    arenaFile.delete();
                }
                plugin.getArenaManager().getArenas().remove(arena.getName());
                player.sendMessage("Arena " + arena.getName() + " has been deleted.");
                player.closeInventory();
                break;
            case "Cancel":
                player.sendMessage("Arena deletion canceled.");
                player.closeInventory();
                break;
        }
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