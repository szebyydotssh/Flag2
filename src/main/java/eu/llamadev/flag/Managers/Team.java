package eu.llamadev.flag.Managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private ChatColor color;
    private List<String> players;
    private Location spawnLocation;
    private Integer teamMaxPlayers;
    private Integer teamMinPlayers;
    private final Arena arena;
    private FlagStatus flagStatus; // New field
    private Location flagLocation;

    public Team(String name, ChatColor color, Integer teamMaxPlayers, Integer teamMinPlayers, Location spawnLocation, Arena arena) {
        this.name = name;
        this.color = color;
        this.players = new ArrayList<>();
        this.spawnLocation = spawnLocation;
        this.teamMaxPlayers = teamMaxPlayers;
        this.teamMinPlayers = teamMinPlayers;
        this.arena = arena;
        this.flagStatus = FlagStatus.ALIVE; // Default status
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void addPlayer(String playerName) {
        if (!players.contains(playerName)) {
            players.add(playerName);
        }
    }

    public Location getFlagLocation() {
        return flagLocation;
    }

    public void setFlagLocation(Location flagLocation) {
        this.flagLocation = flagLocation;
    }

    public void removePlayer(String playerName) {
        players.remove(playerName);
    }

    public boolean isPlayerInTeam(String playerName) {
        return players.contains(playerName);
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

    public Integer getTeamMinPlayers() {
        return teamMinPlayers;
    }

    public void setTeamMinPlayers(Integer teamMinPlayers) {
        this.teamMinPlayers = teamMinPlayers;
    }

    public void setSpawnLocation(Location location) {
        this.spawnLocation = location;
    }

    public Arena getArena() {
        return arena;
    }

    public FlagStatus getFlagStatus() {
        return flagStatus;
    }

    public void setFlagStatus(FlagStatus flagStatus) {
        this.flagStatus = flagStatus;
    }
}