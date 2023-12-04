package me.cantankerousally.hungergames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ScanArenaCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public ScanArenaCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("scanarena")) {
            FileConfiguration config = plugin.getConfig();
            World world = plugin.getServer().getWorld(Objects.requireNonNull(config.getString("region.world")));
            double pos1x = config.getDouble("region.pos1.x");
            double pos1y = config.getDouble("region.pos1.y");
            double pos1z = config.getDouble("region.pos1.z");
            double pos2x = config.getDouble("region.pos2.x");
            double pos2y = config.getDouble("region.pos2.y");
            double pos2z = config.getDouble("region.pos2.z");

            int minX = (int) Math.min(pos1x, pos2x);
            int minY = (int) Math.min(pos1y, pos2y);
            int minZ = (int) Math.min(pos1z, pos2z);
            int maxX = (int) Math.max(pos1x, pos2x);
            int maxY = (int) Math.max(pos1y, pos2y);
            int maxZ = (int) Math.max(pos1z, pos2z);

            File chestLocationsFile = new File(plugin.getDataFolder(), "chest-locations.yml");

            List<Location> chestLocations = new ArrayList<>();
            List<Location> bonusChestLocations = new ArrayList<>();
            List<Location> midChestLocations = new ArrayList<>();
            List<String> bonusChestTypes = config.getStringList("bonus-chest-types");
            List<String> midChestTypes = config.getStringList("mid-chest-types");
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        assert world != null;
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.CHEST) {
                            chestLocations.add(block.getLocation());
                        } else if (bonusChestTypes.contains(block.getType().name())) {
                            bonusChestLocations.add(block.getLocation());
                        } else if (midChestTypes.contains(block.getType().name())) {
                            midChestLocations.add(block.getLocation());
                        }
                    }
                }
            }

            // save the chest locations to the file
            FileConfiguration chestLocationsConfig = new YamlConfiguration();
            chestLocationsConfig.set("locations", chestLocations.stream()
                    .map(Location::serialize)
                    .collect(Collectors.toList()));
            chestLocationsConfig.set("bonus-locations", bonusChestLocations.stream()
                    .map(Location::serialize)
                    .collect(Collectors.toList()));
            chestLocationsConfig.set("mid-locations", midChestLocations.stream()
                    .map(Location::serialize)
                    .collect(Collectors.toList()));
            try {
                chestLocationsConfig.save(chestLocationsFile);
                sender.sendMessage(ChatColor.GREEN + "Chest locations have been saved!");
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Could not save chest locations to file!");
            }
        }
        return true;
    }
}
