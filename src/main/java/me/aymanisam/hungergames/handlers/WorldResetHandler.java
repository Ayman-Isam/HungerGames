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
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class WorldResetHandler {
    private final HungerGames plugin;
    private final ArenaHandler arenaHandler;
    private final ConfigHandler configHandler;
    private final Map<String, BukkitTask> teleportTasks = new HashMap<>();

    public WorldResetHandler(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
        this.configHandler = plugin.getConfigHandler();
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

    public void sendToWorld(World world) {
        for (Player player : world.getPlayers()) {
            String lobbyWorldName = (String) configHandler.createPluginSettings().get("lobby-world");
            assert lobbyWorldName != null;
            World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
            assert lobbyWorld != null;
            player.teleport(lobbyWorld.getSpawnLocation());
        }
    }

    public void resetWorldState(World world) {
        File worldDirectory = world.getWorldFolder();
        File templateDirectory = new File(plugin.getDataFolder(), "templates" + File.separator + world.getName());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!templateDirectory.exists()) {
                Bukkit.getLogger().severe("Template directory does not exist");
                return;
            }

            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }

            arenaHandler.unloadChunks(world);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    boolean isUnloaded = plugin.getServer().unloadWorld(world, false);
                    if (!isUnloaded) {
                        Bukkit.getLogger().severe("World could not be unloaded");
                    }

                    FileUtils.deleteDirectory(worldDirectory);
                    FileUtils.copyDirectory(templateDirectory, worldDirectory);

                    loadWorld(world.getName());

                    BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        World reloadedWorld = Bukkit.getWorld(world.getName());
                        if (reloadedWorld == null) {
                            return;
                        }

                        for (Player player : world.getPlayers()) {
                            player.teleport(reloadedWorld.getSpawnLocation());
                            player.setGameMode(GameMode.ADVENTURE);
                        }

                        Bukkit.getScheduler().cancelTask(teleportTasks.get(world.getName()).getTaskId());
                    }, 0L, 5L);

                    teleportTasks.put(world.getName(), task);

                } catch (IOException e) {
                    Bukkit.getLogger().severe("An error occurred: " + e.getMessage());
                }
            }, 10L);
        }, 10L);
    }

    public static void loadWorld(String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        Bukkit.createWorld(creator);
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
