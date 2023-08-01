package me.cantankerousally.hungergames.commands;

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

public class SupplyDropCommand implements CommandExecutor {
    private JavaPlugin plugin;
    private List<Location> supplyDropLocations;

    public SupplyDropCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("supplydrop")) {
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

            Bukkit.getLogger().info("itemsConfig: " + itemsConfig.saveToString());

            List<ItemStack> supplyDropItems = new ArrayList<>();
            List<Integer> supplyDropItemWeights = new ArrayList<>();
            for (Map<?, ?> itemMap : itemsConfig.getMapList("supply-drop-items")) {
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
                    List<Map<?, ?>> effectsList = (List<Map<?, ?>>) itemMap.get("effects");
                    for (Map<?, ?> effectMap : effectsList) {
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
                supplyDropItems.add(item);
                supplyDropItemWeights.add(weight);

            }
            // Get the supply drop parameters from the config
            int numSupplyDrops = config.getInt("num-supply-drops");
            int minSupplyDropContent = config.getInt("min-supply-drop-content");
            int maxSupplyDropContent = config.getInt("max-supply-drop-content");

            List<String> coords = new ArrayList<>();

            // Generate numSupplyDrops random supply drops
            Random rand = new Random();
            for (int i = 0; i < numSupplyDrops; i++) {
                // Generate random x and z coordinates within the bounds of the arena
                int x = rand.nextInt(maxX - minX + 1) + minX;
                int z = rand.nextInt(maxZ - minZ + 1) + minZ;

                // Get the highest non-air block at the generated coordinates
                Block highestBlock = world.getHighestBlockAt(x, z);

                // Spawn a red shulker box one block above the highest block
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

                // Add items to the shulker box
                int numItems = rand.nextInt(maxSupplyDropContent - minSupplyDropContent + 1) + minSupplyDropContent;
                numItems = Math.min(numItems, supplyDropItems.size());

                // Get numItems random items from the supplyDropItems list
                List<ItemStack> randomItems = new ArrayList<>();
                for (int j = 0; j < numItems; j++) {
                    int index = getRandomWeightedIndex(supplyDropItemWeights);
                    randomItems.add(supplyDropItems.get(index));
                }

                coords.add("(" + x + ", " + highestBlock.getY() + ", " + z + ")");

                // Add the random items to random slots in the shulker box inventory
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
            sb.append(ChatColor.GREEN).append("Spawned ").append(numSupplyDrops).append(" supply drops at ");
            for (int i = 0; i < coords.size(); i++) {
                sb.append(coords.get(i));
                if (i < coords.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("!");

            plugin.getServer().broadcastMessage(sb.toString());
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
