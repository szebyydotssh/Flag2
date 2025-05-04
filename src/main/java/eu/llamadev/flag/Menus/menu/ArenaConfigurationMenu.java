package eu.llamadev.flag.Menus.menu;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Menus.Menu;
import eu.llamadev.flag.Menus.MenuHandler;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArenaConfigurationMenu implements Menu {

    private final Arena arena;
    private final MenuHandler menuHandler;
    private final Flag plugin = Flag.getPlugin(Flag.class);

    public ArenaConfigurationMenu(Arena arena, MenuHandler menuHandler) {
        this.arena = arena;
        this.menuHandler = menuHandler;
    }

    @Override
    public String getMenuName() {
        return "⚙ Setup Arena: " + arena.getName();
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public void open(Player player) {
        System.out.println("Opening ArenaConfigurationMenu for: " + player.getName());
        Inventory inv = Bukkit.createInventory(null, getSlots(), getMenuName());

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < getSlots(); i++) {
            inv.setItem(i, filler);
        }

        // Add menu items with spacing
        inv.setItem(10, createItem(Material.PAPER, "&aSet Max Players", Arrays.asList("&7Current: " + arena.getMaxPlayers())));
        inv.setItem(12, createItem(Material.PAPER, "&aSet Min Players", Arrays.asList("&7Current: " + arena.getMinPlayers())));
        inv.setItem(14, createItem(Material.WOODEN_AXE, "&aSet Positions Tool", Arrays.asList("&7Click to get the tool", "&7for setting positions.")));

        inv.setItem(16, createItem(Material.COMPASS, "&aSet Spectator Spawn", Arrays.asList("&7Click to set the", "&7spectator spawn location.")));
        inv.setItem(20, createItem(Material.RED_BED, "&aSet Lobby Location", Arrays.asList("&7Click to set the", "&7lobby location.")));
        inv.setItem(24, createItem(Material.WHITE_BANNER, "&aTeam Configurations", Arrays.asList("&7Click to configure", "&7teams for this arena.")));

        inv.setItem(22, createItem(Material.BARRIER, "&cDelete Arena", Arrays.asList("&7Click to delete this arena.")));

        player.openInventory(inv);
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) {return;}

        String itemName = Color.strip(clickedItem.getItemMeta().getDisplayName());

        switch (itemName) {
            case "Set Max Players":
                player.sendMessage("Type the maximum number of players in the chat.");
                plugin.getArenaManager().waitingForMaxPlayers.put(player.getUniqueId(), arena);
                player.closeInventory();
                break;
            case "Set Min Players":
                player.sendMessage("Type the minimum number of players in the chat.");
                plugin.getArenaManager().waitingForMinPlayers.put(player.getUniqueId(), arena);
                player.closeInventory();
                break;
            case "Set Positions Tool":
                ItemStack blazeRod = createItem(Material.BLAZE_ROD, "&aPosition Tool", Arrays.asList(
                        "&7Right-click to set Pos1",
                        "&7Left-click to set Pos2",
                        "&7Selected Arena: " + arena.getName()
                ));
                player.getInventory().addItem(blazeRod);
                player.sendMessage("You have been given the Position Tool for arena: " + arena.getName());
                break;
            case "Set Spectator Spawn":
                ItemStack emeraldBlock = createItem(Material.EMERALD_BLOCK, "&aSpectator Spawn Block", Arrays.asList(
                        "&7Place this block to set the",
                        "&7spectator spawn location.",
                        "&7Selected Arena: " + arena.getName()
                ));
                player.getInventory().addItem(emeraldBlock);
                player.sendMessage("You have been given the Spectator Spawn Block for arena: " + arena.getName());
                break;
            case "Set Lobby Location":
                ItemStack goldBlock = createItem(Material.GOLD_BLOCK, "&aLobby Location Block", Arrays.asList(
                        "&7Place this block to set the",
                        "&7lobby location.",
                        "&7Selected Arena: " + arena.getName()
                ));
                player.getInventory().addItem(goldBlock);
                player.sendMessage("You have been given the Lobby Location Block for arena: " + arena.getName());
                break;
            case "Team Configurations":
                menuHandler.openMenu(player, new TeamConfigurationMenu(arena));
                break;
            case "Delete Arena":
                menuHandler.openMenu(player, new ConfirmDeleteMenu(arena));
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