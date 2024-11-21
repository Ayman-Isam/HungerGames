package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

import static me.aymanisam.hungergames.HungerGames.gameStarted;

public class WorldBorderHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;
    private final Map<String, BukkitTask> borderShrinkTask = new HashMap<>();

    public WorldBorderHandler(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.configHandler = plugin.getConfigHandler();
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
        BukkitTask worldBorderShrinkTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (gameStarted.getOrDefault(world.getName(), false)) {
                border.setSize(finalSize, duration);
                for (Player player : world.getPlayers()) {
                    langHandler.getMessage(player, "border.start-shrink");
                }
            } else {
                int borderSize = config.getInt("border.size");
                border.setSize(borderSize);
            }
        }, startTime * 20L);
        borderShrinkTask.put(world.getName(), worldBorderShrinkTask);
    }

    public void resetWorldBorder(World world) {
        BukkitTask worldBorderShrinkTask = borderShrinkTask.get(world.getName());
        if (worldBorderShrinkTask != null) {
            worldBorderShrinkTask.cancel();
        }

        FileConfiguration config = configHandler.getWorldConfig(world);
        int borderSize = config.getInt("border.size");
        WorldBorder border = world.getWorldBorder();
        border.setSize(borderSize);
    }
}
