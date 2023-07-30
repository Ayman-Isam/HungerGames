package me.cantankerousally.hungergames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChestRefillCommand implements CommandExecutor {
    private JavaPlugin plugin;

    public ChestRefillCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chestrefill")) {
            FileConfiguration config = plugin.getConfig();
            World world = plugin.getServer().getWorld(config.getString("region.world"));
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

            FileConfiguration itemsConfig;
            File itemsFile = new File(plugin.getDataFolder(), "items.yml");
            if (itemsFile.exists()) {
                itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
            } else {
                try {
                    itemsConfig = new YamlConfiguration();
                    itemsConfig.set("chest-items", new ArrayList<>());
                    itemsConfig.save(itemsFile);
                    sender.sendMessage(ChatColor.YELLOW + "Created new items.yml file!");
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "Could not create items.yml file!");
                    return true;
                }
            }

            List<ItemStack> chestItems = new ArrayList<>();
            for (Map<?, ?> itemMap : itemsConfig.getMapList("chest-items")) {
                String type = (String) itemMap.get("type");
                ItemStack item = new ItemStack(Material.valueOf(type));
                if (itemMap.containsKey("enchantments")) {
                    for (Map<?, ?> enchantmentMap : (List<Map<?, ?>>) itemMap.get("enchantments")) {
                        String enchantmentType = (String) enchantmentMap.get("type");
                        int level = (int) enchantmentMap.get("level");
                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentType.toLowerCase()));
                        if (enchantment != null) {
                            if (item.getType() == Material.ENCHANTED_BOOK) {
                                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                                meta.addStoredEnchant(enchantment, level, true);
                                item.setItemMeta(meta);
                            } else {
                                item.addEnchantment(enchantment, level);
                            }
                        }
                    }
                }
                chestItems.add(item);
            }

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.CHEST) {
                            Chest chest = (Chest) block.getState();
                            chest.getInventory().clear();
                            chest.getInventory().addItem(chestItems.toArray(new ItemStack[0]));
                        }
                    }
                }
            }

            sender.sendMessage(ChatColor.GREEN + "Chests have been refilled!");
            return true;
        }
        return false;
    }
}
