package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaScanCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ArenaHandler arenaHandler;

    public ArenaScanCommand(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(player.hasPermission("hungergames.scanarena"))) {
            sender.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        FileConfiguration config = arenaHandler.getArenaConfig(player.getWorld());

        if (!config.isSet("region.pos1.x") || !config.isSet("region.pos1.y") || !config.isSet("region.pos1.z") || !config.isSet("region.pos2.x") || !config.isSet("region.pos2.y") || !config.isSet("region.pos2.z")) {
            sender.sendMessage(langHandler.getMessage(player, "scanarena.region-undef"));
            return true;
        }

        World world = player.getWorld();

        arenaHandler.loadChunks(world);

        List<Location> chestLocations = new ArrayList<>();
        List<Location> barrelLocations = new ArrayList<>();
        List<Location> trappedChestLocations = new ArrayList<>();

        File worldFolder = new File(plugin.getDataFolder() + File.separator + world.getName());
        File chestLocationsFile = new File(worldFolder, "chest-locations.yml");

        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState blockState : chunk.getTileEntities()) {
                if (blockState instanceof Chest) {
                    Material type = blockState.getType();
                    if (type == Material.CHEST) {
                        chestLocations.add(blockState.getLocation());
                    } else if (type == Material.TRAPPED_CHEST) {
                        trappedChestLocations.add(blockState.getLocation());
                    }
                } else if (blockState instanceof Barrel) {
                    barrelLocations.add(blockState.getLocation());
                }
            }
        }


        FileConfiguration chestLocationsConfig = new YamlConfiguration();
        chestLocationsConfig.set("chest-locations", chestLocations.stream().map(Location::serialize).collect(Collectors.toList()));
        chestLocationsConfig.set("barrel-locations", barrelLocations.stream().map(Location::serialize).collect(Collectors.toList()));
        chestLocationsConfig.set("trapped-chests-locations", trappedChestLocations.stream().map(Location::serialize).collect(Collectors.toList()));

        try {
            chestLocationsConfig.save(chestLocationsFile);
            sender.sendMessage(langHandler.getMessage(player, "scanarena.saved-locations"));
        } catch (IOException e) {
            sender.sendMessage(langHandler.getMessage(player, "scanarena.failed-locations"));
        }

        player.sendMessage(langHandler.getMessage(player, "scanarena.found-chests", chestLocations.size()));
        player.sendMessage(langHandler.getMessage(player, "scanarena.found-barrels", barrelLocations.size()));
        player.sendMessage(langHandler.getMessage(player, "scanarena.found-trapped-chests", trappedChestLocations.size()));

        return true;
    }
}
