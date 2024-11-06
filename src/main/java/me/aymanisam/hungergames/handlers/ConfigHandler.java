package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class ConfigHandler {
    private final HungerGames plugin;

    private File worldFile;
    private final Map<World, FileConfiguration> worldConfigs = new HashMap<>();

    public ConfigHandler(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
    }

    public void createWorldConfig(World world) {
        String worldName = world.getName();
        worldFile = new File(plugin.getDataFolder() + File.separator + worldName, "config.yml");
        if (!worldFile.exists()) {
            if (!worldFile.getParentFile().mkdirs()) {
                plugin.getLogger().log(Level.SEVERE, "Could not find parent directory for world: " + worldName);
            }
            try {
                plugin.saveResource("config.yml", true);
                Files.copy(new File(plugin.getDataFolder(), "config.yml").toPath(), worldFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create config file for world " + worldName, e);
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(worldFile);
        worldConfigs.put(world, config);
    }

    public FileConfiguration createPluginSettings() {
        File file = new File(plugin.getDataFolder(), "settings.yml");
        if (!file.exists()) {
            plugin.saveResource("settings.yml", true);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getWorldConfig(World world) {
        if (!worldConfigs.containsKey(world)) {
            createWorldConfig(world);
        }
        return worldConfigs.get(world);
    }

    public void saveWorldConfig(World world) {
        FileConfiguration configToSave = getWorldConfig(world);
        File fileToSave = worldFile;
        if (configToSave != null && fileToSave != null) {
            try {
                configToSave.save(fileToSave);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config file for world " + fileToSave.getName(), e);
            }
        }
    }

    public YamlConfiguration loadItemsConfig(World world) {
        String worldName = world.getName();
        File itemsFile = new File(plugin.getDataFolder() + File.separator + worldName, "items.yml");
        if (!itemsFile.exists()) {
            if (!itemsFile.getParentFile().mkdirs()) {
                plugin.getLogger().log(Level.SEVERE, "Could not find parent directory for world: " + worldName);
            }
            plugin.saveResource("items.yml", true);
            try {
                Files.copy(new File(plugin.getDataFolder(), "items.yml").toPath(), itemsFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create items file for world " + worldName, e);
            }
        }
        return YamlConfiguration.loadConfiguration(itemsFile);
    }

    public void loadSignLocations() {
        File signFile = new File(plugin.getDataFolder(), "signs.yml");
        if (!signFile.exists()) {
            plugin.saveResource("signs.yml", true);
            try {
                Files.copy(new File(plugin.getDataFolder(), "signs.yml").toPath(), signFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create sign file for world " + e);
            }
        }
        YamlConfiguration.loadConfiguration(signFile);
    }

    public void validateConfigKeys(World world) {
        YamlConfiguration pluginConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml"))));

        File serverConfigFile = new File(plugin.getDataFolder() + File.separator + world.getName(), "config.yml");

        YamlConfiguration serverConfig = YamlConfiguration.loadConfiguration(serverConfigFile);
        Set<String> keys = pluginConfig.getKeys(true);

        for (String key : keys) {
            if (!serverConfig.isSet(key)) {
                serverConfig.set(key, pluginConfig.get(key));
            }
        }

        try {
            serverConfig.save(serverConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not validate config.yml keys" + e);
        }
    }

    public void validateSettingsKeys() {
        YamlConfiguration pluginSettings = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("settings.yml"))));

        File serverSettingsFile = new File(plugin.getDataFolder(), "settings.yml");

        YamlConfiguration serverSettings = YamlConfiguration.loadConfiguration(serverSettingsFile);
        Set<String> keys = pluginSettings.getKeys(true);

        for (String key : keys) {
            if (!serverSettings.isSet(key)) {
                serverSettings.set(key, pluginSettings.get(key));
            }
        }

        try {
            serverSettings.save(serverSettingsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not validate settings.yml keys" + e);
        }
    }
}
