package eu.llamadev.flag.Menus.menu;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Managers.Team;
import eu.llamadev.flag.Menus.Menu;
import eu.llamadev.flag.Menus.MenuHandler;
import eu.llamadev.flag.Utils.Color;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TeamConfigurationMenu implements Menu {

    private final Arena arena;
    private final MenuHandler menuHandler = Flag.getPlugin(Flag.class).getMenuHandler();
    private final Flag plugin = Flag.getPlugin(Flag.class);

    public TeamConfigurationMenu(Arena arena) {
        this.arena = arena;
    }

    @Override
    public String getMenuName() {
        return "⚙ Teams: " + arena.getName();
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, getSlots(), getMenuName());

        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null, null);
        for (int i = 0; i < getSlots(); i++) {
            inv.setItem(i, filler);
        }

        int index = 0;
        for (Team team : arena.getTeams()) {
            if (index >= getSlots()) break;
            ItemStack teamItem = createItem(Material.WHITE_BANNER, "&a" + team.getName(), List.of(
                    "&7Color: " + team.getColor().name(),
                    "&7Max Players in Team: " + team.getTeamMaxPlayers(),
                    "&7Min Players in Team: " + team.getTeamMinPlayers()
            ), team.getColor());
            inv.setItem(index++, teamItem);
        }

        inv.setItem(31, createItem(Material.EMERALD_BLOCK, "&aAdd Team", List.of("&7Click to add a new team."), null));
        player.openInventory(inv);
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = Color.strip(clickedItem.getItemMeta().getDisplayName());

        if (itemName.equals("Add Team")) {
            player.sendMessage("§aType the team name and color in the chat (e.g., 'Red RED').");
            plugin.getArenaManager().waitingForTeamCreation.put(player.getUniqueId(), arena);
            player.closeInventory();
        } else {
            Team team = arena.getTeamByName(itemName);
            if (team != null) {
                menuHandler.openMenu(player, new TeamSettingsMenu(team));
            }
        }

        e.setCancelled(true);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore, ChatColor teamColor) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        if (mat == Material.WHITE_BANNER && teamColor != null) {
            BannerMeta bannerMeta = (BannerMeta) meta;
            DyeColor dyeColor = getDyeColorFromChatColor(teamColor);
            if (dyeColor != null) {
                bannerMeta.setPatterns(List.of(new Pattern(dyeColor, PatternType.BASE)));
            }
            meta = bannerMeta;
        }

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

    private DyeColor getDyeColorFromChatColor(ChatColor chatColor) {
        try {
            return DyeColor.valueOf(chatColor.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}