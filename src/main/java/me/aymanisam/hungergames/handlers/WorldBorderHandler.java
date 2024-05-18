package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class WorldBorderHandler {
    private final HungerGames plugin;
    private final LangHandler langHandlerInstance;

    public WorldBorderHandler(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandlerInstance = new LangHandler(plugin);
    }

    public void startWorldBorder() {
        FileConfiguration config = plugin.getConfig();
        int startTime = config.getInt("border.start-time");
        int endTime = config.getInt("border.end-time");
        int finalSize = config.getInt("border.final-size");

        // Getting the first world in the server
        World world = plugin.getServer().getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();

        int centerX = config.getInt("border.center-x");
        int centerZ = config.getInt("border.center-z");
        border.setCenter(centerX, centerZ);

        int duration = endTime - startTime;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (HungerGames.gameStarted) {
                border.setSize(finalSize, duration);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    langHandlerInstance.loadLanguageConfig(player);
                    langHandlerInstance.getMessage("border.start-shrink");
                }
            } else {
                int borderSize = config.getInt("border.size");
                border.setSize(borderSize);
            }
        }, startTime * 20L);
    }
}
