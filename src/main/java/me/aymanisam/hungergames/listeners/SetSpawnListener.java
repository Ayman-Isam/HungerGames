package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SetSpawnListener implements Listener {
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ConfigHandler configHandler;
    private final ArenaHandler arenaHandler;
    private final SignClickListener signClickListener;
    private final SignHandler signHandler;

    private final Map<World, Map<Location, BlockData>> originalBlockDataMap = new HashMap<>();

    public SetSpawnListener(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, ArenaHandler arenaHandler) {
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.configHandler = plugin.getConfigHandler();
        this.arenaHandler = arenaHandler;
        this.signClickListener = new SignClickListener(plugin, langHandler, setSpawnHandler, arenaHandler);
        this.signHandler = new SignHandler(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        List<String> worldSpawnPoints = setSpawnHandler.spawnPoints.computeIfAbsent(player.getWorld(), k -> new ArrayList<>());

        if (item != null && item.getType() == Material.STICK && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(langHandler.getMessage(player, "setspawn.stick-name"))) {
            if (!(player.hasPermission("hungergames.setspawn"))) {
                player.sendMessage(langHandler.getMessage(player, "no-permission"));
                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location location = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                FileConfiguration config = configHandler.getWorldConfig(player.getWorld());
                String newSpawnPoint = Objects.requireNonNull(location.getWorld()).getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();


                if (worldSpawnPoints.contains(newSpawnPoint)) {
                    player.sendMessage(langHandler.getMessage(player, "setspawn.duplicate"));
                    event.setCancelled(true);
                    return;
                }

                worldSpawnPoints.add(newSpawnPoint);
                signClickListener.setSignContent(signHandler.loadSignLocations());

                setSpawnHandler.saveSetSpawnConfig(player.getWorld());
                player.sendMessage(langHandler.getMessage(player, "setspawn.pos-set", worldSpawnPoints.size(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                updateGoldBlocksViewForPlayer(player);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Location location = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                String newSpawnPoint = Objects.requireNonNull(location.getWorld()).getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();

                if (!worldSpawnPoints.contains(newSpawnPoint)) {
                    player.sendMessage(langHandler.getMessage(player, "setspawn.not-spawn"));
                    event.setCancelled(true);
                    return;
                }

                player.sendMessage(langHandler.getMessage(player, "setspawn.pos-removed", worldSpawnPoints.size(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                worldSpawnPoints.remove(newSpawnPoint);
                signClickListener.setSignContent(signHandler.loadSignLocations());
                setSpawnHandler.saveSetSpawnConfig(player.getWorld());
                updateGoldBlocksViewForPlayer(player);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
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

        Map<Location, BlockData> worldOriginalBlockDataMap = originalBlockDataMap.computeIfAbsent(event.getPlayer().getWorld(), k -> new HashMap<>());

        worldOriginalBlockDataMap.remove(brokenBlockLocation);
    }

    public void makePlayerSeeGoldBlocks(Player player) {
        List<String> worldSpawnPoints = setSpawnHandler.spawnPoints.computeIfAbsent(player.getWorld(), k -> new ArrayList<>());

        for (String locationStr : worldSpawnPoints) {
            String[] parts = locationStr.split(",");
            if (parts.length == 4) {
                World world = Bukkit.getWorld(parts[0]);
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);

                if (world != null) {
                    Location location = new Location(world, x, y, z);
                    BlockData originalBlockData = world.getBlockAt(location).getBlockData();
                    originalBlockDataMap.computeIfAbsent(world, k -> new HashMap<>()).put(location, originalBlockData);

                    BlockData goldBlockBlockData = Material.GOLD_BLOCK.createBlockData();
                    player.sendBlockChange(location, goldBlockBlockData);
                }
            }
        }
    }

    public void revertPlayerSeeGoldBlocks(Player player) {
        World world = player.getWorld();
        Map<Location, BlockData> worldBlockDataMap = originalBlockDataMap.computeIfAbsent(world, k -> new HashMap<>());

        for (Map.Entry<Location, BlockData> entry : worldBlockDataMap.entrySet()) {
            player.sendBlockChange(entry.getKey(), entry.getValue());
        }
    }

    public void updateGoldBlocksViewForPlayer(Player player) {
        revertPlayerSeeGoldBlocks(player);
        makePlayerSeeGoldBlocks(player);
    }
}
