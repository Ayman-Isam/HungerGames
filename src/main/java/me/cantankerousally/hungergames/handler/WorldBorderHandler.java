package me.cantankerousally.hungergames.handler;

import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldBorderHandler implements Listener {
    private WorldBorder border;

    public WorldBorderHandler(JavaPlugin plugin, World world) {
        border = world.getWorldBorder();
        FileConfiguration config = plugin.getConfig();
        double x = config.getDouble("border.x");
        double z = config.getDouble("border.z");
        double newSize = config.getDouble("border.size");
        border.setCenter(x, z);
        border.setSize(newSize);
    }
}
