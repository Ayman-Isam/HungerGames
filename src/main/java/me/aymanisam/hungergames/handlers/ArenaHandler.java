package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ArenaHandler {
    private final HungerGames plugin;
    private YamlConfiguration arenaConfig;
    private File arenaFile;
    private final LangHandler langHandlerInstance;

    public ArenaHandler(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandlerInstance = new LangHandler(plugin);
    }

    public void createArenaConfig() {
        arenaFile = new File(plugin.getDataFolder(), "arena.yml");
        if (!arenaFile.exists()) {
            arenaFile.getParentFile().mkdirs();
            try {
                plugin.saveResource("arena.yml", false);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, langHandlerInstance.getMessage("arena.create-error"), e);
            }
        }

        try {
            arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, langHandlerInstance.getMessage("arena.load-error"), e);
        }
    }

    public FileConfiguration getArenaConfig() {
        if (arenaConfig == null) {
            createArenaConfig();
            if (arenaConfig == null) {
                plugin.getLogger().log(Level.SEVERE, langHandlerInstance.getMessage("arena.load-error"));
                return null;
            }
        }
        return arenaConfig;
    }

    public void saveArenaConfig() {
        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, langHandlerInstance.getMessage("arena.save-error") + arenaFile, e);
        }
    }
}
