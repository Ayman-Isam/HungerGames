package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        assert world != null;
        WorldBorder border = world.getWorldBorder();

        int numSupplyDrops = config.getInt("num-supply-drops");

        double minX = Math.max(Math.min(arenaConfig.getDouble("region.pos1.x"), arenaConfig.getDouble("region.pos2.x")), border.getCenter().getX() - border.getSize() / 2);
        double minZ = Math.max(Math.min(arenaConfig.getDouble("region.pos1.z"), arenaConfig.getDouble("region.pos2.z")), border.getCenter().getZ() - border.getSize() / 2);
        double maxX = Math.min(Math.max(arenaConfig.getDouble("region.pos1.x"), arenaConfig.getDouble("region.pos2.x")), border.getCenter().getX() + border.getSize() / 2);
        double maxZ = Math.min(Math.max(arenaConfig.getDouble("region.pos1.z"), arenaConfig.getDouble("region.pos2.z")), border.getCenter().getZ() + border.getSize() / 2);

        Random random = new Random();
        for (int i = 0; i < numSupplyDrops; i++) {
            double x, z;
            int highestY;

            do {
                x = minX + (maxX - minX) * random.nextDouble();
                z = minZ + (maxZ - minZ) * random.nextDouble();
                highestY = world.getHighestBlockYAt((int) x, (int) z);
            } while (highestY < -60);

            Block portalBlock = world.getBlockAt((int) x, highestY + 1, (int) z);
            portalBlock.setType(Material.END_GATEWAY);
            Location portalBlockLocation = portalBlock.getLocation().add(0.5, 0.5, 0.5);
            ArmorStand armorStand = (ArmorStand) world.spawnEntity(portalBlockLocation, EntityType.ARMOR_STAND);

            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setCanPickupItems(false);

            Block topmostBlock = world.getBlockAt((int) x, highestY + 2, (int) z);
            topmostBlock.setType(Material.RED_SHULKER_BOX);
            world.playSound(portalBlockLocation, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f);

            int minSupplyDropContent = config.getInt("min-supply-drop-content");
            int maxSupplyDropContent = config.getInt("max-supply-drop-content");

            YamlConfiguration itemsConfig = configHandler.loadItemsConfig();

            List<Location> blockList = new ArrayList<>();
            blockList.add(topmostBlock.getLocation());

            chestRefillHandler.refillInventory(blockList, "supply-drop-items", itemsConfig, minSupplyDropContent, maxSupplyDropContent);

            String message = " X: " + topmostBlock.getX() + " Y: " + topmostBlock.getY() + " Z: " + topmostBlock.getZ();

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                langHandler.getLangConfig(player);
                player.sendMessage(langHandler.getMessage("supplydrop.spawned") + message);
            }
        }
    }
}
