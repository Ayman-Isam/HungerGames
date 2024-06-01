package me.aymanisam.hungergames;

import me.aymanisam.hungergames.handlers.*;
import me.aymanisam.hungergames.listeners.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class HungerGames extends JavaPlugin {

    public static boolean gameStarted = false;
    public static boolean gameStarting = false;

    private GameSequenceHandler gameSequenceHandler;

    @Override
    public void onEnable() {
        int pluginId = 21512;
        Metrics metrics = new Metrics(this, pluginId);
        LangHandler langHandler = new LangHandler(this);
        langHandler.saveLanguageFiles();
        langHandler.loadLanguageConfigs();
        langHandler.updateLanguageKeys();

        // Initializing shared classes
        SetSpawnHandler setSpawnHandler = new SetSpawnHandler(this);
        this.gameSequenceHandler = new GameSequenceHandler(this, setSpawnHandler);

        // Registering command handler
        Objects.requireNonNull(getCommand("hg")).setExecutor(new CommandDispatcher(this, setSpawnHandler, gameSequenceHandler));

        // Registering Handlers
        new CompassHandler(this);
        new WorldBorderHandler(this);
        new ArenaHandler(this);

        // Registering Listeners
        ArenaSelectListener arenaSelectListener = new ArenaSelectListener(this);
        getServer().getPluginManager().registerEvents(arenaSelectListener, this);

        SetSpawnListener setSpawnListener = new SetSpawnListener(this, setSpawnHandler);
        getServer().getPluginManager().registerEvents(setSpawnListener, this);

        SignClickListener signClickListener = new SignClickListener(this, setSpawnHandler);
        getServer().getPluginManager().registerEvents(signClickListener, this);

        PlayerListener playerListener = new PlayerListener(this, setSpawnHandler);
        getServer().getPluginManager().registerEvents(playerListener, this);

        SpectateGuiListener spectateGuiListener = new SpectateGuiListener(this);
        getServer().getPluginManager().registerEvents(spectateGuiListener, this);
    }

    @Override
    public void onDisable() {
        gameSequenceHandler.endGame();
    }

    public File getPluginFile() {
        return this.getFile();
    }
}
