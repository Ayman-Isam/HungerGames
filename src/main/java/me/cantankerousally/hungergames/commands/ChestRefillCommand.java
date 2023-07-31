package me.cantankerousally.hungergames.commands;

import org.bukkit.*;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
            List<Integer> chestItemWeights = new ArrayList<>();
            for (Map<?, ?> itemMap : itemsConfig.getMapList("chest-items")) {
                String type = (String) itemMap.get("type");
                int weight = (int) itemMap.get("weight");
                int amount = itemMap.containsKey("amount") ? (int) itemMap.get("amount") : 1;
                ItemStack item = new ItemStack(Material.valueOf(type), amount);
                if (item.getType() == Material.POTION) {
                    PotionMeta meta = (PotionMeta) item.getItemMeta();
                    String potionType = (String) itemMap.get("potion-type");
                    int level = (int) itemMap.get("level");
                    meta.setBasePotionData(new PotionData(PotionType.valueOf(potionType), false, level > 1));
                    item.setItemMeta(meta);
                } else if (item.getType() == Material.FIREWORK_ROCKET) {
                    FireworkMeta meta = (FireworkMeta) item.getItemMeta();
                    int power = (int) itemMap.get("power");
                    meta.setPower(power);
                    List<Map<String, Object>> effectsList = (List<Map<String, Object>>) itemMap.get("effects");
                    for (Map<String, Object> effectMap : effectsList) {
                        String effectType = (String) effectMap.get("type");
                        List<Integer> colorsList = (List<Integer>) effectMap.get("colors");
                        List<Color> colors = colorsList.stream()
                                .map(color -> Color.fromRGB(color))
                                .collect(Collectors.toList());
                        List<Integer> fadeColorsList = (List<Integer>) effectMap.get("fade-colors");
                        List<Color> fadeColors = fadeColorsList.stream()
                                .map(color -> Color.fromRGB(color))
                                .collect(Collectors.toList());
                        boolean flicker = (boolean) effectMap.get("flicker");
                        boolean trail = (boolean) effectMap.get("trail");
                        FireworkEffect effect = FireworkEffect.builder()
                                .with(FireworkEffect.Type.valueOf(effectType))
                                .withColor(colors)
                                .withFade(fadeColors)
                                .flicker(flicker)
                                .trail(trail)
                                .build();
                        meta.addEffect(effect);
                    }
                    item.setItemMeta(meta);

                } else if (itemMap.containsKey("enchantments")) {
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
                chestItemWeights.add(weight);

            }

            List<ItemStack> bonusChestItems = new ArrayList<>();
            List<Integer> bonusChestItemWeights = new ArrayList<>();
            for (Map<?, ?> itemMap : itemsConfig.getMapList("bonus-chest-items")) {
                String type = (String) itemMap.get("type");
                int weight = (int) itemMap.get("weight");
                int amount = itemMap.containsKey("amount") ? (int) itemMap.get("amount") : 1;
                ItemStack item = new ItemStack(Material.valueOf(type), amount);
                if (item.getType() == Material.POTION) {
                    PotionMeta meta = (PotionMeta) item.getItemMeta();
                    String potionType = (String) itemMap.get("potion-type");
                    int level = (int) itemMap.get("level");
                    meta.setBasePotionData(new PotionData(PotionType.valueOf(potionType), false, level > 1));
                    item.setItemMeta(meta);
                } else if (item.getType() == Material.FIREWORK_ROCKET) {
                    FireworkMeta meta = (FireworkMeta) item.getItemMeta();
                    int power = (int) itemMap.get("power");
                    meta.setPower(power);
                    List<Map<String, Object>> effectsList = (List<Map<String, Object>>) itemMap.get("effects");
                    for (Map<String, Object> effectMap : effectsList) {
                        String effectType = (String) effectMap.get("type");
                        List<Integer> colorsList = (List<Integer>) effectMap.get("colors");
                        List<Color> colors = colorsList.stream()
                                .map(color -> Color.fromRGB(color))
                                .collect(Collectors.toList());
                        List<Integer> fadeColorsList = (List<Integer>) effectMap.get("fade-colors");
                        List<Color> fadeColors = fadeColorsList.stream()
                                .map(color -> Color.fromRGB(color))
                                .collect(Collectors.toList());
                        boolean flicker = (boolean) effectMap.get("flicker");
                        boolean trail = (boolean) effectMap.get("trail");
                        FireworkEffect effect = FireworkEffect.builder()
                                .with(FireworkEffect.Type.valueOf(effectType))
                                .withColor(colors)
                                .withFade(fadeColors)
                                .flicker(flicker)
                                .trail(trail)
                                .build();
                        meta.addEffect(effect);
                    }
                    item.setItemMeta(meta);
                } else if (itemMap.containsKey("enchantments")) {
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
                bonusChestItems.add(item);
                bonusChestItemWeights.add(weight);

            }

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.CHEST) {
                            Chest chest = (Chest) block.getState();
                            chest.getInventory().clear();

                            int minChestContent = config.getInt("min-chest-content");
                            int maxChestContent = config.getInt("max-chest-content");
                            Random rand = new Random();
                            int numItems = rand.nextInt(maxChestContent - minChestContent + 1) + minChestContent;
                            numItems = Math.min(numItems, chestItems.size());


                            // Shuffle the chestItems list and get the first 5 items
                            List<ItemStack> randomItems = new ArrayList<>();
                            for (int i = 0; i < numItems; i++) {
                                int index = getRandomWeightedIndex(chestItemWeights);
                                randomItems.add(chestItems.get(index));
                            }

                            // Add the random items to random slots in the chest inventory
                            Set<Integer> usedSlots = new HashSet<>();
                            for (ItemStack item : randomItems) {
                                int slot = rand.nextInt(chest.getInventory().getSize());
                                while (usedSlots.contains(slot)) {
                                    slot = rand.nextInt(chest.getInventory().getSize());
                                }
                                usedSlots.add(slot);
                                chest.getInventory().setItem(slot, item);
                            }
                        } else {
                            List<String> bonusChestTypes = config.getStringList("bonus-chest-types");
                            if (bonusChestTypes.contains(block.getType().name())) {
                                Inventory bonusChest;
                                if (block.getType() == Material.BARREL) {
                                    Barrel barrel = (Barrel) block.getState();
                                    bonusChest = barrel.getInventory();
                                } else if (block.getType() == Material.TRAPPED_CHEST) {
                                    Chest chest = (Chest) block.getState();
                                    bonusChest = chest.getInventory();
                                } else {
                                    ShulkerBox shulkerBox = (ShulkerBox) block.getState();
                                    bonusChest = shulkerBox.getInventory();
                                }
                                bonusChest.clear();

                                // Get the min and max bonus chest content values from the config
                                int minBonusChestContent = config.getInt("min-bonus-chest-content");
                                int maxBonusChestContent = config.getInt("max-bonus-chest-content");
                                Random rand = new Random();
                                int numItems = rand.nextInt(maxBonusChestContent - minBonusChestContent + 1) + minBonusChestContent;
                                numItems = Math.min(numItems, bonusChestItems.size());

                                List<ItemStack> randomItems = new ArrayList<>();
                                for (int i = 0; i < numItems; i++) {
                                    int index = getRandomWeightedIndex(bonusChestItemWeights);
                                    randomItems.add(bonusChestItems.get(index));
                                }

                                // Add the random items to random slots in the bonus chest inventory
                                Set<Integer> usedSlots = new HashSet<>();
                                for (ItemStack item : randomItems) {
                                    int slot = rand.nextInt(bonusChest.getSize());
                                    while (usedSlots.contains(slot)) {
                                        slot = rand.nextInt(bonusChest.getSize());
                                    }
                                    usedSlots.add(slot);
                                    bonusChest.setItem(slot, item);
                                }
                            }
                        }
                    }
                }
            }

            sender.sendMessage(ChatColor.GREEN + "Chests have been refilled!");
            return true;
        }
        return false;
    }
    private int getRandomWeightedIndex(List<Integer> weights) {
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }
        int randInt = new Random().nextInt(totalWeight);
        for (int i = 0; i < weights.size(); i++) {
            randInt -= weights.get(i);
            if (randInt < 0) {
                return i;
            }
        }
        return -1;
    }
}

