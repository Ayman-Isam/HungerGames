package me.cantankerousally.hungergames.commands;

import me.cantankerousally.hungergames.HungerGames;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class  SupplyDropCommand implements CommandExecutor {
    private final HungerGames plugin;
    Map<String, Color> colorMap = new HashMap<>();

    public SupplyDropCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration getArenaConfig() {
        File arenaFile = new File(plugin.getDataFolder(), "arena.yml");
        if (!arenaFile.exists()) {
            arenaFile.getParentFile().mkdirs();
            plugin.saveResource("arena.yml", false);
        }
        return YamlConfiguration.loadConfiguration(arenaFile);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("supplydrop")) {
            if (sender instanceof Player player) {
                plugin.loadLanguageConfig(player);
                if (player.isOp()) {
                    FileConfiguration config = plugin.getConfig();
                    FileConfiguration arenaConfig = getArenaConfig();
                    String worldName = arenaConfig.getString("region.world");
                    if (worldName == null) {
                        sender.sendMessage(ChatColor.RED + plugin.getMessage("supplydrop.no-arena"));
                        return true;
                    }
                    World world = plugin.getServer().getWorld(worldName);

                    FileConfiguration itemsConfig;
                    File itemsFile = new File(plugin.getDataFolder(), "items.yml");
                    if (itemsFile.exists()) {
                        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
                    } else {
                        try {
                            itemsConfig = new YamlConfiguration();
                            itemsConfig.set("chest-items", new ArrayList<>());
                            itemsConfig.save(itemsFile);
                            sender.sendMessage(ChatColor.YELLOW + plugin.getMessage("supplydrop.created-items"));
                        } catch (IOException e) {
                            sender.sendMessage(ChatColor.RED + plugin.getMessage("supplydrop.failed-items"));
                            return true;
                        }
                    }

                    List<ItemStack> supplyDropItems = new ArrayList<>();
                    List<Integer> supplyDropItemWeights = new ArrayList<>();
                    for (Map<?, ?> itemMap : itemsConfig.getMapList("supply-drop-items")) {
                        String type = (String) itemMap.get("type");
                        int weight = (int) itemMap.get("weight");
                        int amount = itemMap.containsKey("amount") ? (int) itemMap.get("amount") : 1;
                        ItemStack item = new ItemStack(Material.valueOf(type), amount);
                        if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
                            PotionMeta meta = (PotionMeta) item.getItemMeta();
                            String potionType = (String) itemMap.get("potion-type");
                            int level = (int) itemMap.get("level");
                            boolean extended = itemMap.containsKey("extended") && (boolean) itemMap.get("extended");
                            assert meta != null;
                            meta.setBasePotionData(new PotionData(PotionType.valueOf(potionType), extended, level > 1));
                            item.setItemMeta(meta);
                        } else if (item.getType() == Material.FIREWORK_ROCKET) {
                            FireworkMeta meta = (FireworkMeta) item.getItemMeta();
                            int power = (int) itemMap.get("power");
                            assert meta != null;
                            meta.setPower(power);
                            Object effectsObj = itemMap.get("effects");
                            if (effectsObj instanceof List<?> effectsList) {
                                for (Object effectObj : effectsList) {
                                    if (effectObj instanceof Map<?, ?> effectMap) {
                                        String effectType = (String) effectMap.get("type");
                                        Object colorsObj = effectMap.get("colors");
                                        if (colorsObj instanceof List<?> colorsList) {
                                            List<Color> colors = colorsList.stream()
                                                    .filter(String.class::isInstance)
                                                    .map(String.class::cast)
                                                    .map(colorName -> colorMap.getOrDefault(colorName.toUpperCase(), Color.RED))
                                                    .collect(Collectors.toList());
                                            Object fadeColorsObj = effectMap.get("fade-colors");
                                            if (fadeColorsObj instanceof List<?> fadeColorsList) {
                                                List<Color> fadeColors = fadeColorsList.stream()
                                                        .filter(String.class::isInstance)
                                                        .map(String.class::cast)
                                                        .map(colorName -> colorMap.getOrDefault(colorName.toUpperCase(), Color.RED))
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
                                        }
                                    }
                                }
                                item.setItemMeta(meta);
                            }
                        } else if (itemMap.containsKey("enchantments")) {
                            Object enchantmentsObj = itemMap.get("enchantments");
                            if (enchantmentsObj instanceof List<?> enchantmentsList) {
                                for (Object enchantmentObj : enchantmentsList) {
                                    if (enchantmentObj instanceof Map<?, ?> enchantmentMap) {
                                        String enchantmentType = (String) enchantmentMap.get("type");
                                        int level = (int) enchantmentMap.get("level");
                                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentType.toLowerCase()));
                                        if (enchantment != null) {
                                            if (item.getType() == Material.ENCHANTED_BOOK) {
                                                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                                                assert meta != null;
                                                meta.addStoredEnchant(enchantment, level, true);
                                                item.setItemMeta(meta);
                                            } else {
                                                item.addEnchantment(enchantment, level);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        supplyDropItems.add(item);
                        supplyDropItemWeights.add(weight);

                    }

                    int numSupplyDrops = config.getInt("num-supply-drops");
                    int minSupplyDropContent = config.getInt("min-supply-drop-content");
                    int maxSupplyDropContent = config.getInt("max-supply-drop-content");

                    List<String> coords = new ArrayList<>();

                    Random rand = new Random();
                    for (int i = 0; i < numSupplyDrops; i++) {
                        assert world != null;
                        WorldBorder border = world.getWorldBorder();

                        Location center = border.getCenter();
                        double size = border.getSize();

                        double minX = center.getX() - size / 2;
                        double minZ = center.getZ() - size / 2;
                        double maxX = center.getX() + size / 2;
                        double maxZ = center.getZ() + size / 2;

                        int x;
                        int z;
                        Block highestBlock;
                        do {
                            x = (int) (rand.nextDouble() * (maxX - minX) + minX);
                            z = (int) (rand.nextDouble() * (maxZ - minZ) + minZ);
                            highestBlock = world.getHighestBlockAt(x, z);
                        } while (highestBlock.getType() == Material.AIR || highestBlock.getY() < -60);

                        if (highestBlock.getType() == Material.AIR) {
                            continue;
                        }

                        Block block = highestBlock.getRelative(0, 1, 0);
                        block.setType(Material.RED_SHULKER_BOX);
                        ShulkerBox shulkerBox = (ShulkerBox) block.getState();

                        Location location = block.getLocation().add(0.5, 1, 0.5);
                        Firework firework = (Firework) world.spawnEntity(location, EntityType.FIREWORK);
                        FireworkMeta meta = firework.getFireworkMeta();

                        meta.setPower(3); // set the flight duration to 1
                        FireworkEffect effect = FireworkEffect.builder()
                                .with(FireworkEffect.Type.BALL)
                                .withColor(Color.RED)
                                .withFade(Color.ORANGE)
                                .build();
                        meta.addEffect(effect);
                        firework.setFireworkMeta(meta);

                        int numItems = rand.nextInt(maxSupplyDropContent - minSupplyDropContent + 1) + minSupplyDropContent;
                        numItems = Math.min(numItems, supplyDropItems.size());

                        List<ItemStack> randomItems = new ArrayList<>();
                        for (int j = 0; j < numItems; j++) {
                            int index = getRandomWeightedIndex(supplyDropItemWeights);
                            randomItems.add(supplyDropItems.get(index));
                        }

                        coords.add("(" + x + ", " + highestBlock.getY() + ", " + z + ")");

                        Set<Integer> usedSlots = new HashSet<>();
                        for (ItemStack item : randomItems) {
                            int slot = rand.nextInt(shulkerBox.getInventory().getSize());
                            while (usedSlots.contains(slot)) {
                                slot = rand.nextInt(shulkerBox.getInventory().getSize());
                            }
                            usedSlots.add(slot);
                            shulkerBox.getInventory().setItem(slot, item);
                        }
                    }


                    StringBuilder sb = new StringBuilder();
                    sb.append(ChatColor.GREEN)
                            .append(plugin.getMessage("supplydrop.append-1")).append(numSupplyDrops).append(plugin.getMessage("suppplydrop.append-2"));
                    for (int i = 0; i < coords.size(); i++) {
                        sb.append(coords.get(i));
                        if (i < coords.size() - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append("!");

                    plugin.getServer().broadcastMessage(sb.toString());
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + plugin.getMessage("no-permission"));
                }
            }
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
