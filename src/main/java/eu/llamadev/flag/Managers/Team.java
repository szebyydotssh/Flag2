package eu.llamadev.flag.Managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private ChatColor color;
    private List<Player> players;
    private Location spawnLocation;
    private Integer teamMaxPlayers;
    private Integer teamMinPlayers;
    private final Arena arena;

    public Team(String name, ChatColor color, Integer teamMaxPlayers, Integer teamMinPlayers, Location spawnLocation, Arena arena) {
        this.name = name;
        this.color = color;
        this.players = new ArrayList<>();
        this.spawnLocation = null;
        this.teamMaxPlayers = 4;
        this.teamMinPlayers = 1;
        this.arena = arena;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public boolean isPlayerInTeam(Player player) {
        return players.contains(player);
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public Integer getTeamMaxPlayers() {
        return teamMaxPlayers;
    }
    public void setTeamMaxPlayers(Integer teamMaxPlayers) {
        this.teamMaxPlayers = teamMaxPlayers;
    }
    public void setTeamMinPlayers(Integer teamMinPlayers) {
        this.teamMinPlayers = teamMinPlayers;
    }

    public Integer getTeamMinPlayers() {
        return teamMinPlayers;
    }

    public void setSpawnLocation(Location location) {
        this.spawnLocation = location;
    }
    public Arena getArena() {
        return arena;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.teamMaxPlayers = maxPlayers;
    }
    public void setMinPlayers(int minPlayers) {
        this.teamMinPlayers = minPlayers;
    }
}