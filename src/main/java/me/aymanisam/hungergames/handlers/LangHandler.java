package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class LangHandler {
    private final HungerGames plugin;

    private final Map<String, YamlConfiguration> langConfigs = new HashMap<>();

    public LangHandler(HungerGames plugin) {
        this.plugin = plugin;
    }

    public String getMessage(Player player, String key, Object... args) {
        YamlConfiguration langConfig;
        if (player != null) {
            langConfig = getLangConfig(player);
        } else {
            langConfig = getLangConfig();
        }

        String message = langConfig.getString(key);
        if (message != null) {
            for (int i = 0; i < args.length; i++) {
                message = message.replace("{" + i + "}", args[i].toString());
            }
            // Change Minecraft & based colors to bukkit colors
            return ChatColor.translateAlternateColorCodes('&', message);
        }

        plugin.getLogger().log(Level.WARNING, "Missing translation for key: " + key + ". For more information on how to fix this error and update language keys, visit: https://github.com/Ayman-Isam/wiki/Language#language-errors ");
        return (ChatColor.RED + "Missing translation for " + key);
    }

    public void loadLanguageConfigs() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        File[] langFiles = langFolder.listFiles(((dir, name) -> name.endsWith(".yml")));

        if (langFiles == null) {
            saveLanguageFiles();
        }

        assert langFiles != null;
        for (File langFile : langFiles) {
            String locale = langFile.getName().replace(".yml", "");
            YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
            langConfigs.put(locale.toLowerCase(), langConfig);
        }
    }

    public YamlConfiguration getLangConfig(Player player) {
        if (langConfigs.isEmpty()) {
            loadLanguageConfigs();
        }

        String locale = player.getLocale();
        if (langConfigs.containsKey(locale)) {
            return langConfigs.get(locale);
        } else {
            return langConfigs.get("en_us");
        }
    }

    public YamlConfiguration getLangConfig() {
        if (langConfigs.isEmpty()) {
            loadLanguageConfigs();
        }

        YamlConfiguration config = langConfigs.get("en_us");
        if (config == null) {
            config = new YamlConfiguration();
        }

        return config;
    }

    public void saveLanguageFiles() {
        String resourceFolder = "lang";
        File langFolder = new File(plugin.getDataFolder(), resourceFolder);

        // Create a JarFile object from the plugin's file
        try (JarFile jar = new JarFile(plugin.getPluginFile())){
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
        } catch (IOException | SecurityException e) {
            plugin.getLogger().log(Level.SEVERE, "No permission to create folders", e);
        }
    }

    public void validateLanguageKeys() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        File[] langFiles = langFolder.listFiles(((dir, name) -> name.endsWith(".yml")));
        if (langFiles == null) {
            return;
        }

        for (File langFile : langFiles) {
            YamlConfiguration pluginLangConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("lang/en_US.yml"))));
            YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
            boolean updated = false;

            for (String key : pluginLangConfig.getKeys(true)) {
                if (!langConfig.contains(key)) {
                    langConfig.set(key, pluginLangConfig.get(key));
                    updated = true;
                }
            }

            if (updated) {
                try {
                    langConfig.save(langFile);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "No permission to create folders", e);
                }
            }
        }
    }
}
