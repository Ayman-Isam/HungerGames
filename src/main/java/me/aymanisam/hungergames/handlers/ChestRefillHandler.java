package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.*;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ChestRefillHandler {
    private final HungerGames plugin;
    private final ConfigHandler configHandler;
    private final LangHandler langHandler;

    public ChestRefillHandler(HungerGames plugin) {
        this.plugin = plugin;
        this.configHandler = new ConfigHandler(plugin);
        this.langHandler = new LangHandler(plugin);
    }

    public void refillChests() {
        YamlConfiguration itemsConfig = configHandler.loadItemsConfig();
        if (itemsConfig == null) {
            plugin.getLogger().info("Items config is null");
            return;
        }

        File chestLocationFile = new File(plugin.getDataFolder(), "chest-locations.yml");
        FileConfiguration chestLocationsConfig = YamlConfiguration.loadConfiguration(chestLocationFile);

        List<Map<?, ?>> serializedChestLocations = chestLocationsConfig.getMapList("chest-locations");
        List<Map<?, ?>> serializedBarrelLocations = chestLocationsConfig.getMapList("barrel-locations");
        List<Map<?, ?>> serializedTrappedChestLocations = chestLocationsConfig.getMapList("trapped-chests-locations");

        List<Location> chestLocations = serializedChestLocations.stream()
                .map(locationMap -> Location.deserialize((Map<String, Object>) locationMap))
                .collect(Collectors.toList());
        List<Location> barrelLocations = serializedBarrelLocations.stream()
                .map(locationMap -> Location.deserialize((Map<String, Object>) locationMap))
                .collect(Collectors.toList());
        List<Location> trappedChestLocations = serializedTrappedChestLocations.stream()
                .map(locationMap -> Location.deserialize((Map<String, Object>) locationMap))
                .collect(Collectors.toList());

        int minChestContent = plugin.getConfig().getInt("min-chest-content");
        int maxChestContent = plugin.getConfig().getInt("max-chest-content");

        int minBarrelContent = plugin.getConfig().getInt("min-barrel-content");
        int maxBarrelContent = plugin.getConfig().getInt("max-barrel-content");

        int minTrappedChestContent = plugin.getConfig().getInt("min-trapped-chest-content");
        int maxTrappedChestContent = plugin.getConfig().getInt("max-trapped-chest-content");

        refillInventory(chestLocations, "chest-items", itemsConfig, minChestContent, maxChestContent);
        refillInventory(barrelLocations, "barrel-items", itemsConfig, minBarrelContent, maxBarrelContent);
        refillInventory(trappedChestLocations, "trapped-chest-items", itemsConfig, minTrappedChestContent, maxTrappedChestContent);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            langHandler.getLangConfig(player);
            player.sendMessage(langHandler.getMessage("chestrefill.refilled"));
        }
    }

    public void refillInventory(List<Location> locations, String itemKey, YamlConfiguration itemsConfig, int minContent, int maxContent) {
        for (Location location : locations) {
            Block block = location.getBlock();
            Inventory blockInventory;

            if (block.getState() instanceof Chest chest) {
                blockInventory = chest.getInventory();
            } else if (block.getState() instanceof Barrel barrel) {
                blockInventory = barrel.getInventory();
            } else if (block.getState() instanceof ShulkerBox shulkerBox) {
                blockInventory = shulkerBox.getInventory();
            } else {
                continue;
            }

            List<Map<?, ?>> itemsMapList = (List<Map<?, ?>>) itemsConfig.getList(itemKey);

            assert itemsMapList != null;
            List<ItemStack> items = itemsMapList.stream()
                    .flatMap(itemMap -> {
                        String type = (String) itemMap.get("type");

                        String meta = (itemMap.get("meta") != null) ? (String) itemMap.get("meta") : null;

                        Integer amountObj = (Integer) itemMap.get("amount");
                        int amount = (amountObj != null) ? amountObj : 1;

                        Integer weightObj = (Integer) itemMap.get("weight");
                        int weight = (weightObj != null) ? weightObj : 1;

                        ItemStack item;

                        if (type.equals("POTION") || type.equals("SPLASH_POTION") || type.equals("LINGERING_POTION") || type.equals("TIPPED_ARROW")) {
                            item = new ItemStack(Objects.requireNonNull(Material.getMaterial(type)), amount);
                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta != null && meta!= null) {
                                itemMeta.setDisplayName(meta);
                            }
                            PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                            String potionType = (String) itemMap.get("potion-type");
                            Integer levelObj = (Integer) itemMap.get("level");
                            int level = (levelObj != null) ? levelObj : 1;
                            boolean extended = itemMap.containsKey("extended") && (boolean) itemMap.get("extended");
                            assert potionMeta != null;
                            potionMeta.setBasePotionData(new PotionData(PotionType.valueOf(potionType), extended, level > 1));
                            item.setItemMeta(potionMeta);
                        } else if (itemMap.containsKey("enchantments")) {
                            Material material = Material.getMaterial(type);
                            assert material != null;
                            item = new ItemStack(material, amount);
                            Object enchantsObj = itemMap.get("enchantments");
                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta != null && meta!= null) {
                                itemMeta.setDisplayName(meta);
                            }
                            if (enchantsObj instanceof List<?> enchantList) {
                                for (Object enchantObj : enchantList) {
                                    if (enchantObj instanceof Map<?, ?> enchantMap) {
                                        String enchantmentType = (String) enchantMap.get("type");
                                        int level = (int) enchantMap.get("level");
                                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentType.toLowerCase()));
                                        if (enchantment != null) {
                                            if (material == Material.ENCHANTED_BOOK) {
                                                EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) item.getItemMeta();
                                                assert enchantmentStorageMeta != null;
                                                enchantmentStorageMeta.addStoredEnchant(enchantment, level, true);
                                                item.setItemMeta(enchantmentStorageMeta);
                                            } else {
                                                item.addUnsafeEnchantment(enchantment, level);
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (type.equals("FIREWORK_ROCKET")) {
                            item = new ItemStack(Material.FIREWORK_ROCKET, amount);
                            FireworkMeta fireworkMeta = (FireworkMeta) item.getItemMeta();
                            assert fireworkMeta != null;
                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta != null && meta!= null) {
                                itemMeta.setDisplayName(meta);
                            }
                            fireworkMeta.setPower((Integer) itemMap.get("power"));

                            List<Map<?, ?>> effectsList = (List<Map<?, ?>>) itemMap.get("effects");
                            for (Map<?, ?> effectMap : effectsList) {
                                FireworkEffect.Type effectType = FireworkEffect.Type.valueOf((String) effectMap.get("type"));
                                List<Color> colors = ((List<String>) effectMap.get("colors")).stream().map(this::getColorByName).collect(Collectors.toList());
                                List<Color> fadeColors = ((List<String>) effectMap.get("fade-colors")).stream().map(this::getColorByName).collect(Collectors.toList());
                                boolean flicker = (boolean) effectMap.get("flicker");
                                boolean trail = (boolean) effectMap.get("trail");

                                FireworkEffect effect = FireworkEffect.builder()
                                        .with(effectType)
                                        .withColor(colors)
                                        .withFade(fadeColors)
                                        .flicker(flicker)
                                        .trail(trail)
                                        .build();

                                fireworkMeta.addEffect(effect);
                            }

                            item.setItemMeta(fireworkMeta);
                        } else {
                            Material material = Material.getMaterial(type);
                            item = new ItemStack(material, amount);
                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta != null && meta!= null) {
                                itemMeta.setDisplayName(meta);
                            }
                        }
                        return Collections.nCopies(weight, item).stream();
                    })
                    .collect(Collectors.toList());

            blockInventory.clear();

            Random rand = new Random();
            int inventorySize = rand.nextInt(maxContent - minContent + 1) + minContent;
            Collections.shuffle(items);

            inventorySize = Math.min(inventorySize, items.size());

            int addedItems = 0;
            int totalSlots = blockInventory.getSize();

            while (addedItems < inventorySize) {
                int randomSlot = rand.nextInt(totalSlots);
                if (blockInventory.getItem(randomSlot) == null) {
                    blockInventory.setItem(randomSlot, items.get(addedItems));
                    addedItems++;
                }
            }
        }
    }

    public Color getColorByName(String colorName) {
        return switch (colorName.toUpperCase()) {
            case "ORANGE" -> Color.ORANGE;
            case "MAGENTA", "PINK" -> Color.FUCHSIA;
            case "LIGHT_BLUE" -> Color.AQUA;
            case "YELLOW" -> Color.YELLOW;
            case "LIME" -> Color.LIME;
            case "GRAY" -> Color.GRAY;
            case "LIGHT_GRAY" -> Color.SILVER;
            case "CYAN" -> Color.TEAL;
            case "PURPLE" -> Color.PURPLE;
            case "BLUE" -> Color.BLUE;
            case "BROWN" -> Color.MAROON;
            case "GREEN" -> Color.GREEN;
            case "RED" -> Color.RED;
            case "BLACK" -> Color.BLACK;
            default -> Color.WHITE;
        };
    }
}
