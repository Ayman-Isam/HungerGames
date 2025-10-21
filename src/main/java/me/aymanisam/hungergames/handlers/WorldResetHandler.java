package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EndGateway;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class WorldResetHandler {
    private final HungerGames plugin;
    private final WorldBorderHandler worldBorderHandler;

    public WorldResetHandler(HungerGames plugin, WorldBorderHandler worldBorderHandler) {
        this.plugin = plugin;
	    this.worldBorderHandler = worldBorderHandler;
    }

    public void saveWorldState(World world) {
        File worldDirectory = world.getWorldFolder();
        File templateDirectory = new File(plugin.getDataFolder(), "templates" + File.separator + world.getName());

        if (!templateDirectory.exists()) {
            if (!templateDirectory.mkdirs()) {
                plugin.getLogger().log(Level.SEVERE, "Could not create templates directory");
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                FileUtils.copyDirectory(worldDirectory, templateDirectory, pathname -> {
                    String name = pathname.getName();
                    return !name.equals("session.lock") && !name.equals("uid.dat") && !name.equals("session.dat");
                });
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not copy world folders");
            }
        });
    }

    public void resetWorldState(World world) {
        File worldDirectory = world.getWorldFolder();
        File templateDirectory = new File(plugin.getDataFolder(), "templates" + File.separator + world.getName());

	    if (!templateDirectory.exists()) {
		    Bukkit.getLogger().severe("Template directory does not exist");
		    return;
	    }

        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean unloaded = Bukkit.unloadWorld(world, false);
            if (!unloaded) {
                plugin.getLogger().log(Level.SEVERE, "Could not unload world");
                return;
            }

            new BukkitRunnable() {
                int attempts = 0;

                @Override
                public void run() {
                    try {
                        FileUtils.deleteDirectory(worldDirectory);
                        FileUtils.copyDirectory(templateDirectory, worldDirectory);
                        WorldCreator worldCreator = new WorldCreator(world.getName());
                        Bukkit.createWorld(worldCreator);
                        worldBorderHandler.resetWorldBorder(world);
                        cancel();
                    } catch (IOException e) {
                        attempts++;

                        if (attempts >= 40) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to reset world '" + world.getName() + "' after " + attempts + " attempts.", e);
                            cancel();
                        }
                    }
                }
            }.runTaskTimer(plugin, 2L, 2L);
        });
    }

    public void removeShulkers(World world) {
        NamespacedKey supplyDropKey = new NamespacedKey(plugin, "supplydrop");

        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                if (state instanceof ShulkerBox shulkerBox) {
                    PersistentDataContainer dataContainer = shulkerBox.getPersistentDataContainer();

                    if (dataContainer.has(supplyDropKey, PersistentDataType.STRING) &&
                            "true".equals(dataContainer.get(supplyDropKey, PersistentDataType.STRING))) {

                        Block block = state.getBlock();
                        block.setType(Material.AIR);
                    }
                } else if (state instanceof EndGateway) {
                    Block block = state.getBlock();
                    block.setType(Material.AIR);
                }
            }
        }

        for (Entity entity : world.getEntities()) {
            if (entity instanceof ArmorStand armorStand) {
                PersistentDataContainer dataContainer = armorStand.getPersistentDataContainer();

                if (dataContainer.has(supplyDropKey, PersistentDataType.STRING) && "true".equals(dataContainer.get(supplyDropKey, PersistentDataType.STRING))) {
                    armorStand.remove();
                }
            }
        }
    }
}
