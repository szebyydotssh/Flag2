package eu.llamadev.flag.Listeners;

import eu.llamadev.flag.Flag;
import eu.llamadev.flag.Managers.Arena;
import eu.llamadev.flag.Managers.FlagStatus;
import eu.llamadev.flag.Managers.GameState;
import eu.llamadev.flag.Managers.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FlagStolingListener implements Listener {

    private final Flag plugin = Flag.getPlugin(Flag.class);
    private final Map<Player, ItemStack> savedHelmets = new HashMap<>();

    @EventHandler
    public void onFlagBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location blockLocation = event.getBlock().getLocation();

        Arena arena = plugin.getArenaManager().getArenaByPlayer(player);
        if (arena == null || plugin.getGameManager().getGameState() != GameState.INGAME) return;

        Team team = arena.getTeams().stream()
                .filter(t -> t.getFlagLocation() != null && t.getFlagLocation().equals(blockLocation))
                .findFirst()
                .orElse(null);

        if (team == null || team.getFlagStatus() != FlagStatus.ALIVE) return;
        if(team == arena.getTeamByPlayer(player.getName())) {
            player.sendMessage("§cYou cannot steal your own flag!");
            return;
        }

        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);

        team.setFlagStatus(FlagStatus.STOLEN);
        plugin.getGameManager().addFlagCarrier(player, team);
        savedHelmets.put(player, player.getInventory().getHelmet());
        ItemStack banner = new ItemStack(Material.WHITE_BANNER);
        banner.setItemMeta(plugin.getGameManager().createBannerMeta(team.getColor()));
        player.getInventory().setHelmet(banner);

        player.sendMessage("§cYou have stolen the flag of team: " + team.getName() + "!");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Team team = plugin.getGameManager().getFlagCarrierTeam(player);

        if( team == null || team.getFlagStatus() != FlagStatus.STOLEN) return;
        if(!(plugin.getGameManager().isFlagCarrier(player))) return;

            // Restore the flag to its original location
            team.setFlagStatus(FlagStatus.ALIVE);
            Location stolenFlagLocation = team.getFlagLocation();
            stolenFlagLocation.getBlock().setType(Material.WHITE_BANNER); // Set the block to a white banner

            // Remove the flag carrier
            plugin.getGameManager().removeFlagCarrier(player);

            // Notify players
            Bukkit.broadcastMessage("§cThe flag of team " + team.getName() + " has been restored!");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Team team = plugin.getGameManager().getFlagCarrierTeam(player);

        if (team != null && team.getFlagStatus() == FlagStatus.STOLEN) {
            Arena arena = team.getArena();
            if (arena == null) return;

            Location flagLocation = arena.getTeamByPlayer(player.getName()).getFlagLocation();
            double distance = player.getLocation().distance(flagLocation);

            if (distance <= 5) { // Adjust the radius as needed
                team.setFlagStatus(FlagStatus.BROKEN);
                plugin.getGameManager().updateArenaScoreboard(arena);
                Bukkit.broadcastMessage("§cThe flag of team " + team.getName() + " has been broken by " + player.getName() + "!");
                player.getInventory().setHelmet(savedHelmets.get(player));
                savedHelmets.remove(player);
                plugin.getGameManager().removeFlagCarrier(player);
            }
        }
    }
}