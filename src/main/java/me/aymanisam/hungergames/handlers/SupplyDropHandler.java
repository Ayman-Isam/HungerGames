package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class SupplyDropHandler {
    private final HungerGames plugin;
    private final ConfigHandler configHandler;
    private final ArenaHandler arenaHandler;
    private final ChestRefillHandler chestRefillHandler;
    private final LangHandler langHandler;

    public SupplyDropHandler(HungerGames plugin) {
        this.plugin = plugin;
        this.configHandler = new ConfigHandler(plugin);
        this.arenaHandler = new ArenaHandler(plugin);
        this.chestRefillHandler = new ChestRefillHandler(plugin);
        this.langHandler = new LangHandler(plugin);
    }

    public void setSupplyDrop() {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration arenaConfig = arenaHandler.getArenaConfig();

        World world = plugin.getServer().getWorld(Objects.requireNonNull(arenaConfig.getString("region.world")));
        WorldBorder border = world.getWorldBorder();

        int numSupplyDrops = config.getInt("num-supply-drops");

        Location center = border.getCenter();

        double minX = center.getX() - border.getSize() / 2;
        double minZ = center.getZ() - border.getSize() / 2;
        double maxX = center.getX() + border.getSize() / 2;
        double maxZ = center.getZ() + border.getSize() / 2;

        Random random = new Random();
        for (int i = 0; i < numSupplyDrops; i++) {
            double x = minX + (maxX - minX) * random.nextDouble();
            double z = minZ + (maxZ - minZ) * random.nextDouble();

            int highestY = world.getHighestBlockYAt((int) x, (int) z);
            Block topmostBlock = world.getBlockAt((int) x, highestY + 1, (int) z);

            topmostBlock.setType(Material.RED_SHULKER_BOX);

            int minSupplyDropContent = config.getInt("min-supply-drop-content");
            int maxSupplyDropContent = config.getInt("max-supply-drop-content");

            YamlConfiguration itemsConfig = configHandler.loadItemsConfig();

            List<Location> blockList = new ArrayList<>();
            blockList.add(topmostBlock.getLocation());

            chestRefillHandler.refillInventory(blockList, "supply-drop-items", itemsConfig, minSupplyDropContent, maxSupplyDropContent);

            String message = " X: " + topmostBlock.getX() + " Y: " + topmostBlock.getY() + " Z: " + topmostBlock.getZ();

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                langHandler.loadLanguageConfig(player);
                player.sendMessage(langHandler.getMessage("supplydrop.spawned") + message);
            }
        }
    }
}
