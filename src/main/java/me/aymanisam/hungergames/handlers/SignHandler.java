package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class SignHandler {
    private final HungerGames plugin;
    private final File file;
    private final FileConfiguration config;

    public SignHandler(HungerGames plugin) {
        this.plugin = plugin;

        file = new File(plugin.getDataFolder(), "signs.yml");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to create sign file");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create sign.yml", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveSignLocations(List<Location> signLocations) {
        List<String> locations = new ArrayList<>();
        for (Location location : signLocations) {
            String locString = Objects.requireNonNull(location.getWorld()).getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
            locations.add(locString);
        }
        config.set("signs", locations);
        saveConfig();
    }

    public List<Location> loadSignLocations() {
        List<Location> signLocations = new ArrayList<>();
        List<String> locations = config.getStringList("signs");
        for (String locString : locations) {
            String[] parts = locString.split(",");
            World world = Bukkit.getWorld(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            signLocations.add(new Location(world, x, y, z));
        }
        return signLocations;
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save sign.yml", e);
        }
    }
}
