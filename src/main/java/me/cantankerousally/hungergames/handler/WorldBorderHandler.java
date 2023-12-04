package me.cantankerousally.hungergames.handler;

import me.cantankerousally.hungergames.HungerGames;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldBorderHandler implements Listener {
    private final JavaPlugin plugin;

    public WorldBorderHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public void startBorderShrink() {
        FileConfiguration config = plugin.getConfig();
        long startTime = config.getLong("border.start-time");
        long endTime = config.getLong("border.end-time");
        double finalSize = config.getDouble("border.final-size");

        World world = plugin.getServer().getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();

        double centerX = config.getDouble("border.center-x");
        double centerZ = config.getDouble("border.center-z");
        border.setCenter(centerX, centerZ);

        long duration = endTime - startTime;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (((HungerGames) plugin).gameStarted) {
                border.setSize(finalSize, duration);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.GOLD + "The world border has started to shrink!");
                }
            } else {
                double borderSize = plugin.getConfig().getDouble("border.size");
                border.setSize(borderSize);
            }
        }, startTime * 20);
    }
}
