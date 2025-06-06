package eu.llamadev.flag;

import eu.llamadev.flag.Commands.CommandManager;
import eu.llamadev.flag.Configurations.Messages;
import eu.llamadev.flag.Listeners.*;
import eu.llamadev.flag.Managers.ArenaManager;
import eu.llamadev.flag.Managers.GameManager;
import eu.llamadev.flag.Menus.MenuHandler;
import eu.llamadev.flag.Menus.menu.ArenaConfigurationMenu;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Flag extends JavaPlugin {

    private PluginDescriptionFile pdf = getDescription();
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private MenuHandler menuHandler;


    Logger l = Bukkit.getLogger();

    @Override
    public void onEnable() {
        l.info("");
        l.info("&cFLAG &f- &cv" + pdf.getVersion());
        l.info("");
        l.info("&cStatus: &fStarted");
        l.info("");
        getCommand("flag").setExecutor(new CommandManager());
        this.arenaManager = new ArenaManager();
        this.gameManager = new GameManager(this);
        menuHandler = new MenuHandler(this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new MenuHandler(this), this);
        getServer().getPluginManager().registerEvents(new PositionToolListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new TeamCreationListener(), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(), this);
        getServer().getPluginManager().registerEvents(new LuckyBlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new FlagStolingListener(), this);

        arenaManager.loadArenas();
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        Messages.setup();
        Messages.get().addDefault("config-version", "1.0");
        Messages.get().options().copyDefaults(true);
        Messages.save();
    }

    @Override
    public void onDisable() {
        l.info("");
        l.info("&cFLAG &f- &cv" + pdf.getVersion());
        l.info("");
        l.info("&cStatus: &fDisabled");
        l.info("");
        cleanup();
    }

    public ArenaManager getArenaManager() { return this.arenaManager; }
    public MenuHandler getMenuHandler() { return this.menuHandler; }

    public void cleanup() {
        ArenaManager arenaManager = getArenaManager();
        arenaManager.getArenas().clear();

        ArenaManager.waitingForMaxPlayers.clear();
        ArenaManager.waitingForMinPlayers.clear();
        arenaManager.waitingForMaxPlayersT.clear();
        arenaManager.waitingForMinPlayersT.clear();
        arenaManager.waitingForNameChange.clear();
        arenaManager.waitingForTeamCreation.clear();
        arenaManager.waitingForTeamSpawn.clear();

        getLogger().info("All resources have been cleaned up.");
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }
}
