package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class ConfigHandler {
    private final HungerGames plugin;

    public ConfigHandler(HungerGames plugin) {
        this.plugin = plugin;
    }

    public YamlConfiguration loadItemsConfig() {
        File itemsFile = new File(plugin.getDataFolder(), "items.yml");
        if (itemsFile.exists()) {
            return YamlConfiguration.loadConfiguration(itemsFile);
        } else {
            plugin.getLogger().log(Level.SEVERE, "Items file not found.");
            return null;
        }
    }

    public void checkConfigKeys() {
        YamlConfiguration pluginConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml"))));
        File serverConfigFile = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration serverConfig = YamlConfiguration.loadConfiguration(serverConfigFile);
        Set<String> keys = pluginConfig.getKeys(true);

        for (String key : keys) {
            if (!serverConfig.contains(key)) {
                serverConfig.set(key, pluginConfig.get(key));
                plugin.getLogger().warning("&cMissing key: " + key);
            }
        }

        try {
            serverConfig.save(serverConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
