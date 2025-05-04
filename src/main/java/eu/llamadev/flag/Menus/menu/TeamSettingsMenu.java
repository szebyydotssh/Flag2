package eu.llamadev.flag.Menus.menu;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Team;
import eu.llamadev.flag.Menus.Menu;
import eu.llamadev.flag.Menus.MenuHandler;
import eu.llamadev.flag.Utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TeamSettingsMenu implements Menu {

    private final Team team;
    private final Flag plugin = Flag.getPlugin(Flag.class);

    public TeamSettingsMenu(Team team) {
        this.team = team;
    }

    @Override
    public String getMenuName() {
        return "⚙ Team Settings: " + team.getName();
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, getSlots(), getMenuName());

        // Filler items
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < getSlots(); i++) {
            inv.setItem(i, filler);
        }

        // Add menu options
        inv.setItem(10, createItem(Material.COMPASS, "&aSet Team Spawn", List.of("&7Click to set the team's spawn.")));
        inv.setItem(12, createItem(Material.PLAYER_HEAD, "&aSet Max Players", List.of("&7Click to set the max players.")));
        inv.setItem(14, createItem(Material.PLAYER_HEAD, "&aSet Min Players", List.of("&7Click to set the min players.")));
        inv.setItem(16, createItem(Material.ANVIL, "&aChange Team Name/Color", List.of("&7Click to change the team's name or color.")));
        inv.setItem(22, createItem(Material.BARRIER, "&cDelete Team", List.of("&7Click to delete this team.")));

        player.openInventory(inv);
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = Color.strip(clickedItem.getItemMeta().getDisplayName());

        switch (itemName) {
            case "Set Team Spawn":
                player.closeInventory();
                player.sendMessage("§aRight-click to set the team's spawn location.");
                plugin.getArenaManager().setTeamSpawn(player, team);
                break;
            case "Set Max Players":
                player.closeInventory();
                player.sendMessage("§aType the max players in chat.");
                plugin.getArenaManager().waitingForMaxPlayersT.put(player.getUniqueId(), team);
                break;
            case "Set Min Players":
                player.closeInventory();
                player.sendMessage("§aType the min players in chat.");
                plugin.getArenaManager().waitingForMinPlayersT.put(player.getUniqueId(), team);
                break;
            case "Change Team Name/Color":
                player.closeInventory();
                player.sendMessage("§aType the new team name and color in chat (e.g., 'Blue BLUE').");
                plugin.getArenaManager().waitingForNameChange.put(player.getUniqueId(), team);
                break;
            case "Delete Team":
                player.closeInventory();
                plugin.getArenaManager().deleteTeam(team.getArena().getName(), team.getName());
                player.sendMessage("§cTeam deleted successfully.");
                break;
        }

        e.setCancelled(true);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.translate(name));
        if (lore != null) {
            meta.setLore(lore.stream().map(Color::translate).toList());
        }
        item.setItemMeta(meta);
        return item;
    }
}