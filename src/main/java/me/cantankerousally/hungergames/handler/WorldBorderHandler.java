package me.cantankerousally.hungergames.handler;

import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldBorderHandler implements Listener {
    private JavaPlugin plugin;

    public WorldBorderHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfigValues() {
        FileConfiguration config = plugin.getConfig();
        double x = config.getDouble("border.x");
        double z = config.getDouble("border.z");
        double size = config.getDouble("border.size");

        World world = plugin.getServer().getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();
        border.setCenter(x, z);
        border.setSize(size);
    }
    public void startBorderShrink() {
        FileConfiguration config = plugin.getConfig();
        long startTime = config.getLong("border.start-time");
        long endTime = config.getLong("border.end-time");
        double finalSize = config.getDouble("border.final-size");

        World world = plugin.getServer().getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();

        long duration = endTime - startTime;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            border.setSize(finalSize, duration);
        }, startTime * 20);
    }
}
