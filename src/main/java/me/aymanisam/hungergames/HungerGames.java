package me.aymanisam.hungergames;

import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.CompassHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.WorldBorderHandler;
import me.aymanisam.hungergames.listeners.ArenaSelectListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class HungerGames extends JavaPlugin {

    public static boolean gameStarted;

    @Override
    public void onEnable() {
        int pluginId = 21512;
        Metrics metrics = new Metrics(this, pluginId);
        LangHandler langHandler = new LangHandler(this);
        langHandler.saveLanguageFiles();

        // Registering command handler
        Objects.requireNonNull(getCommand("hg")).setExecutor(new CommandHandler(this));

        // Registering Handlers
        new CompassHandler(this);
        new WorldBorderHandler(this);
        new ArenaHandler(this);

        // Registering Listeners
        ArenaSelectListener arenaSelectListener = new ArenaSelectListener(langHandler, this);
        getServer().getPluginManager().registerEvents(arenaSelectListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public File getPluginFile() {
        return this.getFile();
    }
}
