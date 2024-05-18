package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class LangHandler {
    private final HungerGames plugin;
    private YamlConfiguration langConfig;

    public LangHandler(HungerGames plugin) {
        this.plugin = plugin;
    }

    public String getMessage(String key) {
        if (langConfig != null) {
            String message = langConfig.getString(key);
            if (message != null) {
                // Change Minecraft & based colors to bukkit colors
                return ChatColor.translateAlternateColorCodes('&', message);
            }
        }
        plugin.getLogger().log(Level.WARNING, "Missing translation for key: " + key + ". For more information on how to fix this error and update language keys, visit: https://github.com/Ayman-Isam/wiki/Language#language-errors ");
        return (ChatColor.RED + "Missing translation for " + key);
    }

    public void loadLanguageConfig(Player player) {
        String locale = player.getLocale();
        File langFile = new File(plugin.getDataFolder(), "lang/" + locale + ".yml");
        File defaultlangFile = new File(plugin.getDataFolder(), "lang/" + "en_US" + ".yml");
        if (langFile.exists()) {
            langConfig = YamlConfiguration.loadConfiguration(langFile);
        } else {
            // If the locale of the player is not supported, en_US is loaded
            langConfig = YamlConfiguration.loadConfiguration(defaultlangFile);
        }
    }

    public void saveLanguageFiles() {
        String resourceFolder = "lang";
        File langFolder = new File(plugin.getDataFolder(), resourceFolder);
        if (!langFolder.exists()) {
            if (!langFolder.mkdir()) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create language folder");
                // May fail to create language on first install due to plugin folder not existing
                plugin.getLogger().log(Level.SEVERE, "Please Restart the Server");
            }
        }

        try {
            // Create a JarFile object from the plugin's file
            JarFile jar = new JarFile(plugin.getPluginFile());
            Enumeration<JarEntry> entries = jar.entries();

            // Iterate over each entry in the JAR file
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(resourceFolder + "/") && entry.getName().endsWith(".yml")) {
                    String fileName = new File(entry.getName()).getName();
                    File langFile = new File(langFolder, fileName);
                    if (!langFile.exists()) {
                        plugin.saveResource(resourceFolder + "/" + fileName, false);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
