package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class WorldBorderHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;

    public WorldBorderHandler(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.configHandler = new ConfigHandler(plugin);
    }

    public void startWorldBorder(World world) {
        FileConfiguration config = configHandler.getWorldConfig(world);
        int startTime = config.getInt("border.start-time");
        int endTime = config.getInt("border.end-time");
        int finalSize = config.getInt("border.final-size");

        WorldBorder border = world.getWorldBorder();

        int centerX = config.getInt("border.center-x");
        int centerZ = config.getInt("border.center-z");
        border.setCenter(centerX, centerZ);

        int duration = endTime - startTime;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (HungerGames.gameStarted) {
                border.setSize(finalSize, duration);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    langHandler.getLangConfig(player);
                    langHandler.getMessage("border.start-shrink");
                }
            } else {
                int borderSize = config.getInt("border.size");
                border.setSize(borderSize);
            }
        }, startTime * 20L);
    }

    public void resetWorldBorder(World world) {
        FileConfiguration config = configHandler.getWorldConfig(world);
        int borderSize = config.getInt("border.size");
        WorldBorder border = world.getWorldBorder();
        border.setSize(borderSize);
    }
}
