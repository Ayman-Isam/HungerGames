package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;

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
    private YamlConfiguration langConfig;

    private final Map<String, YamlConfiguration> langConfigs = new HashMap<>();

    public LangHandler(HungerGames plugin) {
        this.plugin = plugin;
    }

    public String getMessage(String key, Object... args) {
        if (langConfig == null) {
            File defaultLangFile = new File(plugin.getDataFolder(), "lang/en_US.yml");
            langConfig = YamlConfiguration.loadConfiguration(defaultLangFile);
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
            langConfigs.put(locale, langConfig);
        }
    }

    public YamlConfiguration getLangConfig (Player player) {
        String locale = player.getLocale();
        if (langConfigs.containsKey(locale)) {
            return langConfigs.get(locale);
        } else {
            return langConfigs.get("en_US");
        }
    }

    public void saveLanguageFiles() {
        String resourceFolder = "lang";
        File langFolder = new File(plugin.getDataFolder(), resourceFolder);

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
            e.printStackTrace();
        }
    }

    public void updateLanguageKeys() {
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
                    e.printStackTrace();
                }
            }
        }
    }
}
