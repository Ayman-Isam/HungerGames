package me.aymanisam.hungergames;

import me.aymanisam.hungergames.handlers.CompassHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.WorldBorderHandler;
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
        LangHandler langHandlerInstance = new LangHandler(this);
        langHandlerInstance.saveLanguageFiles();

        // Registering command handler
        Objects.requireNonNull(getCommand("hg")).setExecutor(new CommandHandler(this));

        // Registering Handlers
        new CompassHandler(this);
        new WorldBorderHandler(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public File getPluginFile() {
        return this.getFile();
    }
}
