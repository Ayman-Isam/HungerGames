package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetSpawnListener implements Listener {
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ConfigHandler configHandler;

    private final Map<Location, BlockData> originalBlockDataMap = new HashMap<>();

    public SetSpawnListener(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler) {
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ;
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.STICK && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(langHandler.getMessage(player, "setspawn.stick-name"))) {
            if (!(player.hasPermission("hungergames.setspawn"))) {
                player.sendMessage(langHandler.getMessage(player, "no-permission"));
                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location location = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                FileConfiguration config = configHandler.getWorldConfig(player.getWorld());
                String newSpawnPoint = Objects.requireNonNull(location.getWorld()).getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();

                if (setSpawnHandler.spawnPoints.contains(newSpawnPoint)) {
                    player.sendMessage(langHandler.getMessage(player, "setspawn.duplicate"));
                    event.setCancelled(true);
                    return;
                }

                if (setSpawnHandler.spawnPoints.size() >= config.getInt("max-players")) {
                    player.sendMessage(langHandler.getMessage(player, "setspawn.max-spawn"));
                    event.setCancelled(true);
                    return;
                }

                setSpawnHandler.spawnPoints.add(newSpawnPoint);
                setSpawnHandler.saveSetSpawnConfig();
                player.sendMessage(langHandler.getMessage(player, "setspawn.pos-set", setSpawnHandler.spawnPoints.size(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                updateGoldBlocksViewForPlayer(player);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Location location = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                String newSpawnPoint = Objects.requireNonNull(location.getWorld()).getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();

                if (!setSpawnHandler.spawnPoints.contains(newSpawnPoint)) {
                    player.sendMessage(langHandler.getMessage(player, "setspawn.not-spawn"));
                    event.setCancelled(true);
                    return;
                }

                player.sendMessage(langHandler.getMessage(player, "setspawn.pos-removed", setSpawnHandler.spawnPoints.size(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                setSpawnHandler.spawnPoints.remove(newSpawnPoint);
                setSpawnHandler.saveSetSpawnConfig();
                updateGoldBlocksViewForPlayer(player);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ;
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item != null && item.getType() == Material.STICK && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(langHandler.getMessage(player, "setspawn.stick-name"))) {
            if (!(player.hasPermission("hungergames.setspawn"))) {
                player.sendMessage(langHandler.getMessage(player, "no-permission"));
                return;
            }
            makePlayerSeeGoldBlocks(player);
        } else {
            revertPlayerSeeGoldBlocks(player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location brokenBlockLocation = event.getBlock().getLocation();
        originalBlockDataMap.remove(brokenBlockLocation);
    }

    public void makePlayerSeeGoldBlocks (Player player) {
        for (String locationStr : setSpawnHandler.spawnPoints) {
            String[] parts = locationStr.split(",");
            if (parts.length == 4) {
                World world = Bukkit.getWorld(parts[0]);
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);

                if (world != null) {
                    Location location = new Location(world, x, y, z);
                    BlockData originalBlockData = world.getBlockAt(location).getBlockData();
                    originalBlockDataMap.put(location, originalBlockData);

                    BlockData goldBlockBlockData = Material.GOLD_BLOCK.createBlockData();
                    player.sendBlockChange(location, goldBlockBlockData);
                }
            }
        }
    }

    public void revertPlayerSeeGoldBlocks(Player player) {
        for (Map.Entry<Location, BlockData> entry : originalBlockDataMap.entrySet()) {
            player.sendBlockChange(entry.getKey(), entry.getValue());
        }
    }

    public void updateGoldBlocksViewForPlayer(Player player) {
        revertPlayerSeeGoldBlocks(player);
        makePlayerSeeGoldBlocks(player);
    }
}
