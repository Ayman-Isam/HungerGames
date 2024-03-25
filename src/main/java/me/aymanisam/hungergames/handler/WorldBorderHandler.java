package me.aymanisam.hungergames.handler;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;


public class WorldBorderHandler implements Listener {
    private final HungerGames plugin;

    public WorldBorderHandler(HungerGames plugin) {
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
            if (plugin.gameStarted) {
                border.setSize(finalSize, duration);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    plugin.loadLanguageConfig(player);
                    player.sendMessage(plugin.getMessage("borderhandler.start-shrink"));
                }
            } else {
                double borderSize = plugin.getConfig().getDouble("border.size");
                border.setSize(borderSize);
            }
        }, startTime * 20);
    }
}
