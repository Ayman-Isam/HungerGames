package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.listeners.ArenaSelectListener;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EndGateway;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ArenaHandler {
    private final HungerGames plugin;
    private YamlConfiguration arenaConfig;
    private File arenaFile;
    private final LangHandler langHandler;
    private final ArenaSelectListener arenaSelectListener;

    public ArenaHandler(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.arenaSelectListener = new ArenaSelectListener(plugin, langHandler);
    }

    public void createArenaConfig(World world) {
        String worldName = world.getName();
        arenaFile = new File(plugin.getDataFolder() + File.separator + worldName, "arena.yml");
        if (!arenaFile.exists()) {
            arenaFile.getParentFile().mkdirs();
            File tempFile = new File(plugin.getDataFolder(), "arena.yml");
            try {
                plugin.saveResource("arena.yml", true);
                if(tempFile.exists()) {
                    tempFile.renameTo(arenaFile);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create arena.yml from", e);
            }
        }

        try {
            arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load arena.yml from", e);
        }
    }

    public FileConfiguration getArenaConfig(World world) {
        createArenaConfig(world);
        if (arenaConfig == null) {
            plugin.getLogger().log(Level.SEVERE, "Could not load arena.yml from");
            return null;
        }
        return arenaConfig;
    }

    public void saveArenaConfig() {
        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save arena.yml to" + arenaFile, e);
        }
    }

    private List<Chunk> getChunksToLoadOrUnload(World world) {
        List<Chunk> chunks = new ArrayList<>();

        arenaConfig = (YamlConfiguration) getArenaConfig(world);

        if (arenaConfig == null) {
            plugin.getLogger().log(Level.SEVERE, "Arena config is not initialized properly for world" + world.getName());
            return chunks;
        }

        String worldName = arenaConfig.getString("region.world");

        if (worldName == null) {
            plugin.getLogger().log(Level.SEVERE, "World name is not specified in the arena config for world: " + world.getName());
            return chunks;
        }

        double pos1x = arenaConfig.getDouble("region.pos1.x");
        double pos1z = arenaConfig.getDouble("region.pos1.z");
        double pos2x = arenaConfig.getDouble("region.pos2.x");
        double pos2z = arenaConfig.getDouble("region.pos2.z");

        int minX = (int) Math.min(pos1x, pos2x);
        int minZ = (int) Math.min(pos1z, pos2z);
        int maxX = (int) Math.max(pos1x, pos2x);
        int maxZ = (int) Math.max(pos1z, pos2z);

        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                chunks.add(world.getChunkAt(chunkX, chunkZ));
            }
        }

        return chunks;
    }

    public void loadChunks(World world) {
        List<Chunk> chunks = getChunksToLoadOrUnload(world);
        for (Chunk chunk : chunks) {
            if (!chunk.isLoaded()) {
                chunk.load();
            }
            chunk.setForceLoaded(true);
        }
    }

    public void unloadChunks(World world) {
        List<Chunk> chunks = getChunksToLoadOrUnload(world);
        for (Chunk chunk : chunks) {
            chunk.setForceLoaded(false);
            if (chunk.isLoaded()) {
                chunk.unload();
            }
        }
    }
}
