package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

import static me.aymanisam.hungergames.commands.SignSetCommand.slots;
import static me.aymanisam.hungergames.handlers.SignHandler.signLocations;

public class ConfigHandler {
    private final HungerGames plugin;

    private File worldFile;
	private File signFile;
    private final Map<String, FileConfiguration> worldConfigs = new HashMap<>();
    private FileConfiguration pluginSettings;

    public ConfigHandler(HungerGames plugin) {
        this.plugin = plugin;
    }

    public void createWorldConfig(World world) {
        String worldName = world.getName();
        worldFile = new File(plugin.getDataFolder() + File.separator + worldName, "config.yml");

        File parentDirectory = worldFile.getParentFile();
        if (!parentDirectory.exists()) {
            if (!parentDirectory.mkdirs()) {
                plugin.getLogger().log(Level.SEVERE, "Could not find parent directory for world: " + worldName);
                return;
            }
        }

        if (!worldFile.exists()) {
            try {
                plugin.saveResource("config.yml", true);
                Files.copy(new File(plugin.getDataFolder(), "config.yml").toPath(), worldFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create config file for world " + worldName, e);
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(worldFile);
        worldConfigs.put(world.getName(), config);
    }

    public void createPluginSettings() {
        File file = new File(plugin.getDataFolder(), "settings.yml");
        if (!file.exists()) {
            plugin.saveResource("settings.yml", true);
        }

        pluginSettings = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getPluginSettings() {
        if (pluginSettings == null) {
            createPluginSettings();
        }
        return pluginSettings;
    }

    public FileConfiguration getWorldConfig(World world) {
        if (!worldConfigs.containsKey(world.getName())) {
            createWorldConfig(world);
        }
        return worldConfigs.get(world.getName());
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

        File parentDirectory = itemsFile.getParentFile();
        if (!parentDirectory.exists()) {
            if (!parentDirectory.mkdirs()) {
                plugin.getLogger().log(Level.SEVERE, "Could not find parent directory for world: " + worldName);
                return null;
            }
        }

        if (!itemsFile.exists()) {
            try {
                plugin.saveResource("items.yml", true);
                Files.copy(new File(plugin.getDataFolder(), "items.yml").toPath(), itemsFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create items file for world " + worldName, e);
            }
        }

        return YamlConfiguration.loadConfiguration(itemsFile);
    }

    public FileConfiguration loadSignFile() {
        signFile = new File(plugin.getDataFolder(), "signs.yml");
        if (!signFile.exists()) {
            plugin.saveResource("signs.yml", true);
            try {
                Files.copy(new File(plugin.getDataFolder(), "signs.yml").toPath(), signFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create sign file for world " + e);
            }
        }
        return YamlConfiguration.loadConfiguration(signFile);
    }

	public void saveSignLocations() {
		FileConfiguration config = loadSignFile();
		List<String> locations = new ArrayList<>();
		for (Map.Entry<String, Location> entry : signLocations.entrySet()) {
			Location location = entry.getValue();
			String locString = Objects.requireNonNull(location.getWorld()).getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + entry.getKey();
			locations.add(locString);
		}
		config.set("signs", locations);
		try {
			config.save(signFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not save sign.yml", e);
		}
	}

	public void loadSignLocations() {
		List<String> locations = loadSignFile().getStringList("signs");
		for (String locString : locations) {
			String[] parts = locString.split(",");
			World world = Bukkit.getWorld(parts[0]);
			double x = Double.parseDouble(parts[1]);
			double y = Double.parseDouble(parts[2]);
			double z = Double.parseDouble(parts[3]);
			// TODO Add check for people updating to new sign system
			String slot = parts[4];
			signLocations.put(slot, new Location(world, x, y, z));
		}
	}

	public void loadSlots() {
		for (String slot : loadSignFile().getStringList("slots")) {
			String[] parts = slot.split(",");
			slots.put(parts[0], parts[1]);
		}
	}

	public void setSlots() {
		List<String> items = new ArrayList<>();
		FileConfiguration config = loadSignFile();

		for (Map.Entry<String, String> entry : slots.entrySet()) {
			items.add(entry.getKey() + "," + entry.getValue());
		}

		config.set("slots", items);

		try {
			config.save(signFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not save sign.yml", e);
		}
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
