package eu.llamadev.flag.Managers;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Arena {

    private String name;
    private final List<Team> teams;
    private Location pos1;
    private Location pos2;
    private Location spectatorLocation;
    private Location lobbyLocation;
    private ArrayList players;
    private Integer maxPlayers;
    private Integer minPlayers;

    public Arena(String name, Location pos1, Location pos2, Location spectatorLocation, Location lobbyLocation, Integer maxPlayers, Integer minPlayers) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.spectatorLocation = spectatorLocation;
        this.lobbyLocation = lobbyLocation;
        this.players = new ArrayList();
        this.teams = new ArrayList<>();
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setSpectatorLocation(Location spectatorLocation) {
        this.spectatorLocation = spectatorLocation;
    }

    public Location getSpectatorLocation() {
        return spectatorLocation;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public ArrayList getPlayers() {
        return players;
    }
    public void addPlayer(String name) {
        players.add(name);
    }
    public void removePlayer(String name) {
        players.remove(name);
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }

    public void setSpectatorSpawn(Location location) {
        this.spectatorLocation = location;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void addTeam(Team team) {
        teams.add(team);
    }

    public void removeTeam(Team team) {
        teams.remove(team);
    }

    public Team getTeamByName(String teamName) {
        for (Team team : teams) {
            if (team.getName().equalsIgnoreCase(teamName)) {
                return team;
            }
        }
        return null;
    }
    public Team getTeamByPlayer(String playerName) {
        for (Team team : teams) {
            if (team.getPlayers().contains(playerName)) {
                return team;
            }
        }
        return null;
    }

    public boolean teamExists(String teamName) {
        for (Team team : teams) {
            if (team.getName().equalsIgnoreCase(teamName)) {
                return true;
            }
        }
        return false;
    }
}
