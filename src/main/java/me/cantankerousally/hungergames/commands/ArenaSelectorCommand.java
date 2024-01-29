package me.cantankerousally.hungergames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArenaSelectorCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private FileConfiguration arenaConfig = null;
    private File arenaFile = null;


    public ArenaSelectorCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        createArenaConfig();
    }

    public void createArenaConfig() {
        arenaFile = new File(plugin.getDataFolder(), "arena.yml");
        if (!arenaFile.exists()) {
            arenaFile.getParentFile().mkdirs();
            plugin.saveResource("arena.yml", false);
        }

        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
    }

    public FileConfiguration getArenaConfig() {
        if (arenaConfig == null) {
            createArenaConfig();
        }
        return arenaConfig;
    }

    public void saveArenaConfig() {
        try {
            getArenaConfig().save(arenaFile);
        } catch (IOException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not save arena.yml to " + arenaFile, e);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("select")) {
            if (sender instanceof Player player) {
                ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
                ItemMeta meta = blazeRod.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.AQUA + "Arena Selector");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.LIGHT_PURPLE + "Left-click to select position 1");
                lore.add(ChatColor.LIGHT_PURPLE + "Right-click to select position 2");
                meta.setLore(lore);
                blazeRod.setItemMeta(meta);
                player.getInventory().addItem(blazeRod);
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "You have been given an Arena Selector!");
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be executed by players.");
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("create")) {
            if (sender instanceof Player player) {
                if (player.hasMetadata("arena_pos1") && player.hasMetadata("arena_pos2")) {
                    Location pos1 = (Location) player.getMetadata("arena_pos1").get(0).value();
                    Location pos2 = (Location) player.getMetadata("arena_pos2").get(0).value();
                    if (pos1 != null && pos2 != null) {
                        getArenaConfig().set("region.world", Objects.requireNonNull(pos1.getWorld()).getName());
                        getArenaConfig().set("region.pos1.x", pos1.getX());
                        getArenaConfig().set("region.pos1.y", pos1.getY());
                        getArenaConfig().set("region.pos1.z", pos1.getZ());
                        getArenaConfig().set("region.pos2.x", pos2.getX());
                        getArenaConfig().set("region.pos2.y", pos2.getY());
                        getArenaConfig().set("region.pos2.z", pos2.getZ());
                        File itemsFile = new File (plugin.getDataFolder(), "items.yml");
                        if (!itemsFile.exists()) {
                            saveArenaConfig();
                            sender.sendMessage(ChatColor.GREEN + "Region created and saved to arena.yml!");
                        }
                        sender.sendMessage(ChatColor.GREEN + "Region created and saved to arena.yml!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid position values.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You must set both positions first using the Arena Selector!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be executed by players.");
            }
            return true;
        }
        return false;
    }
}