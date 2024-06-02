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
    private ArenaHandler arenaHandler;

    @Override
    public void onEnable() {
        int pluginId = 21512;
        Metrics metrics = new Metrics(this, pluginId);
        LangHandler langHandler = new LangHandler(this);
        langHandler.saveLanguageFiles();
        langHandler.updateLanguageKeys();

        // Initializing shared classes
        SetSpawnHandler setSpawnHandler = new SetSpawnHandler(this);
        new CompassHandler(this);
        this.gameSequenceHandler = new GameSequenceHandler(this, setSpawnHandler);
        this.arenaHandler = new ArenaHandler(this);

        // Registering command handler
        Objects.requireNonNull(getCommand("hg")).setExecutor(new CommandDispatcher(this, setSpawnHandler, gameSequenceHandler));

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

        arenaHandler.loadChunks();
    }

    @Override
    public void onDisable() {
        gameSequenceHandler.endGame();
    }

    public File getPluginFile() {
        return this.getFile();
    }
}
