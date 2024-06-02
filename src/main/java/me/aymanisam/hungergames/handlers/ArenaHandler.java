package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public class ArenaHandler {
    private final HungerGames plugin;
    private YamlConfiguration arenaConfig;
    private File arenaFile;
    private final LangHandler langHandler;

    public ArenaHandler(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        createArenaConfig();
    }

    public void createArenaConfig() {
        arenaFile = new File(plugin.getDataFolder(), "arena.yml");
        if (!arenaFile.exists()) {
            arenaFile.getParentFile().mkdirs();
            try {
                plugin.saveResource("arena.yml", false);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, langHandler.getMessage("arena.create-error"), e);
            }
        }

        try {
            arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, langHandler.getMessage("arena.load-error"), e);
        }
    }

    public FileConfiguration getArenaConfig() {
        if (arenaConfig == null) {
            createArenaConfig();
            if (arenaConfig == null) {
                plugin.getLogger().log(Level.SEVERE, langHandler.getMessage("arena.load-error"));
                return null;
            }
        }
        return arenaConfig;
    }

    public void saveArenaConfig() {
        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, langHandler.getMessage("arena.save-error") + arenaFile, e);
        }
    }

    public void loadChunks() {
        World world = plugin.getServer().getWorld(Objects.requireNonNull(arenaConfig.getString("region.world")));
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
                assert world != null;
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    world.loadChunk(chunkX, chunkZ);
                }
                world.setChunkForceLoaded(chunkX, chunkZ, true);
            }
        }
    }

    public void removeShulkers() {
        World world = plugin.getServer().getWorld(Objects.requireNonNull(arenaConfig.getString("region.world")));

        assert world != null;
        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState blockState : chunk.getTileEntities()) {
                if (blockState instanceof ShulkerBox) {
                    blockState.setType(Material.AIR);
                    blockState.update(true);
                }
            }
        }
    }
}
