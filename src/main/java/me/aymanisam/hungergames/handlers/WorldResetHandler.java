package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.*;
import org.apache.commons.io.FileUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EndGateway;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class WorldResetHandler {
    private final HungerGames plugin;
    private final ArenaHandler arenaHandler;

    public WorldResetHandler(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
    }

    public void saveWorldState(World world) {
        File worldDirectory = world.getWorldFolder();
        File templateDirectory = new File(plugin.getDataFolder(), "templates" + File.separator + world.getName());

        if (!templateDirectory.exists()) {
            templateDirectory.mkdirs();
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                FileUtils.copyDirectory(worldDirectory, templateDirectory, pathname -> {
                    String name = pathname.getName();
                    return !name.equals("session.lock") && !name.equals("uid.dat") && !name.equals("session.dat");
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void resetWorldState(World world) {
        File worldDirectory = world.getWorldFolder();
        File templateDirectory = new File(plugin.getDataFolder(), "templates" + File.separator + world.getName());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!templateDirectory.exists()) {
                Bukkit.getLogger().severe("Template directory does not exist");
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                World voidWorld = Bukkit.getWorld("voidworld");
                assert voidWorld != null;
                player.teleport(voidWorld.getSpawnLocation());
                player.setGameMode(GameMode.SPECTATOR);
            }

            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }

            arenaHandler.unloadChunks(world);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    Bukkit.getLogger().info("Starting to unload world");

                    boolean isUnloaded = plugin.getServer().unloadWorld(world, false);
                    if (!isUnloaded) {
                        Bukkit.getLogger().severe("World could not be unloaded");
                    }

                    Bukkit.getLogger().info("World unloaded");

                    Bukkit.getLogger().info("Starting to delete world directory");
                    FileUtils.deleteDirectory(worldDirectory);
                    Bukkit.getLogger().info("World directory deleted");

                    Bukkit.getLogger().info("Starting to copy template directory to world directory");
                    FileUtils.copyDirectory(templateDirectory, worldDirectory);
                    Bukkit.getLogger().info("Template directory copied to world directory");

                    Bukkit.getLogger().info("Starting to create new world");
                    WorldCreator creator = new WorldCreator(world.getName());
                    World newWorld = Bukkit.createWorld(creator);
                    Bukkit.getLogger().info("New world created");

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.getLogger().info("Starting to teleport players and set their game mode");
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            assert newWorld != null;
                            player.teleport(newWorld.getSpawnLocation());
                            player.setGameMode(GameMode.ADVENTURE);
                        }
                        Bukkit.getLogger().info("Players teleported and game mode set");
                    });

                } catch (IOException e) {
                    Bukkit.getLogger().severe("An error occurred: " + e.getMessage());
                }
            }, 40L);
        }, 40L);
    }

    public void removeShulkers(World world) {
        NamespacedKey supplyDropKey = new NamespacedKey(plugin, "supplydrop");

        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                if (state instanceof ShulkerBox) {
                    ShulkerBox shulkerBox = (ShulkerBox) state;
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
            if (entity instanceof ArmorStand) {
                ArmorStand armorStand = (ArmorStand) entity;
                PersistentDataContainer dataContainer = armorStand.getPersistentDataContainer();

                if (dataContainer.has(supplyDropKey, PersistentDataType.STRING) &&
                        "true".equals(dataContainer.get(supplyDropKey, PersistentDataType.STRING))) {

                    armorStand.remove();
                }
            }
        }
    }
}
