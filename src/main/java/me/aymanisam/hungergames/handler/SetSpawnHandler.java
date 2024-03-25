package me.aymanisam.hungergames.handler;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.commands.JoinGameCommand;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class SetSpawnHandler implements Listener {

    private final HungerGames plugin;
    private FileConfiguration setSpawnConfig = null;
    private File setSpawnFile = null;
    private final PlayerSignClickManager playerSignClickManager;
    private JoinGameCommand joinGameCommand;

    public SetSpawnHandler(HungerGames plugin, PlayerSignClickManager playerSignClickManager, JoinGameCommand joinGameCommand) {
        this.plugin = plugin;
        this.playerSignClickManager = playerSignClickManager;
        this.joinGameCommand = joinGameCommand;
        createSetSpawnConfig();
        createArenaConfig();
    }

    public void setJoinGameCommand(JoinGameCommand joinGameCommand) {
        this.joinGameCommand = joinGameCommand;
    }

    private final Set<String> occupiedSpawnPoints = new HashSet<>();
    private final Map<Player, String> playerSpawnPoints = new HashMap<>();


    public void createSetSpawnConfig() {
        setSpawnFile = new File(plugin.getDataFolder(), "setspawn.yml");
        if (!setSpawnFile.exists()) {
            setSpawnFile.getParentFile().mkdirs();
            plugin.saveResource("setspawn.yml", false);
        }

        setSpawnConfig = YamlConfiguration.loadConfiguration(setSpawnFile);
    }

    public void createArenaConfig() {
        File arenaFile = new File(plugin.getDataFolder(), "arena.yml");
        if (!arenaFile.exists()) {
            arenaFile.getParentFile().mkdirs();
            plugin.saveResource("arena.yml", false);
        }

        FileConfiguration arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
    }

    public FileConfiguration getSetSpawnConfig() {
        if (setSpawnConfig == null) {
            createSetSpawnConfig();
        }
        return setSpawnConfig;
    }

    public void saveSetSpawnConfig() {
        if (setSpawnConfig == null || setSpawnFile == null) {
            return;
        }
        try {
            getSetSpawnConfig().save(setSpawnFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.OFF, plugin.getMessage("setspawnhandler.failed-save") + setSpawnFile, ex);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        plugin.loadLanguageConfig(player);
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.STICK && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            if (meta.getDisplayName().equals(plugin.getMessage("setspawn.stick-name"))) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    Location location = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                    List<String> spawnPoints = getSetSpawnConfig().getStringList("spawnpoints");
                    FileConfiguration config = plugin.getConfig();
                    String newSpawnPoint = Objects.requireNonNull(location.getWorld()).getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
                    if (spawnPoints.contains(newSpawnPoint)) {
                        player.sendMessage(plugin.getMessage("setspawnhandler.duplicate"));
                        event.setCancelled(true);
                    }
                    if (spawnPoints.size() < config.getInt("max-players")) {
                        spawnPoints.add(newSpawnPoint);
                        getSetSpawnConfig().set("spawnpoints", spawnPoints);
                        saveSetSpawnConfig();
                        player.sendMessage(plugin.getMessage("setspawnhandler.position-set") + spawnPoints.size() + plugin.getMessage("setspawnhandler.set-at") + location.getBlockX() + plugin.getMessage("setspawnhandler.coord-y") + location.getBlockY() + plugin.getMessage("setspawnhandler.coord-z") + location.getBlockZ());
                    } else if (spawnPoints.size() ==  config.getInt("max-players")){
                        player.sendMessage(ChatColor.RED + plugin.getMessage("setspawnhandler.max-spawn"));
                    }
                    event.setCancelled(true);
                }
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            assert block != null;
            if (block.getState() instanceof Sign sign) {
                if (sign.getLine(0).equalsIgnoreCase("[Join]")) {
                    if (plugin.gameStarted) {
                        player.sendMessage(ChatColor.RED + plugin.getMessage("setspawnhandler.game-started"));
                        return;
                    }
                    playerSignClickManager.setPlayerSignClicked(player, true);
                    List<String> spawnPoints = getSetSpawnConfig().getStringList("spawnpoints");
                    List<String> availableSpawnPoints = new ArrayList<>(spawnPoints);
                    availableSpawnPoints.removeAll(occupiedSpawnPoints);
                    if (!availableSpawnPoints.isEmpty()) {
                        String spawnPoint = availableSpawnPoints.get(new Random().nextInt(availableSpawnPoints.size()));
                        String[] coords = spawnPoint.split(",");
                        World world = plugin.getServer().getWorld(coords[0]);
                        double x = Double.parseDouble(coords[1]) + 0.5;
                        double y = Double.parseDouble(coords[2]) + 1.0;
                        double z = Double.parseDouble(coords[3]) + 0.5;
                        player.teleport(new Location(world, x, y, z));
                        occupiedSpawnPoints.add(spawnPoint);
                        joinGameCommand.addPlayerToGame(player);
                        player.setGameMode(GameMode.ADVENTURE);
                        player.getInventory().clear();
                        player.setExp(0);
                        player.setLevel(30);
                        player.setHealth(20);
                        player.setFoodLevel(20);
                        for (PotionEffect effect : player.getActivePotionEffects()) {
                            player.removePotionEffect(effect.getType());
                        }
                        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                            plugin.loadLanguageConfig(onlinePlayer);
                            onlinePlayer.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GRAY + plugin.getMessage("setspawnhandler.joined-message-1") + ChatColor.DARK_GREEN + plugin.getMessage("setspawnhandler.joined-message-2") + occupiedSpawnPoints.size() + plugin.getMessage("setspawnhandler.joined-message-3") + spawnPoints.size() + plugin.getMessage("setspawnhandler.joined-message-4"));
                        }
                        playerSpawnPoints.put(player, spawnPoint);
                    } else {
                        player.sendMessage(ChatColor.RED + plugin.getMessage("setspawnhandler.spawn-filled"));
                    }
                }
            }
        }
    }

    public boolean handleJoin(Player player) {
        plugin.loadLanguageConfig(player);
        if (plugin.gameStarted) {
            player.sendMessage(ChatColor.RED + plugin.getMessage("setspawnhandler.game-started"));
            return false;
        }
        playerSignClickManager.setPlayerSignClicked(player, true);
        List<String> spawnPoints = getSetSpawnConfig().getStringList("spawnpoints");
        List<String> availableSpawnPoints = new ArrayList<>(spawnPoints);
        availableSpawnPoints.removeAll(occupiedSpawnPoints);
        if (!availableSpawnPoints.isEmpty()) {
            String spawnPoint = availableSpawnPoints.get(new Random().nextInt(availableSpawnPoints.size()));
            String[] coords = spawnPoint.split(",");
            World world = plugin.getServer().getWorld(coords[0]);
            double x = Double.parseDouble(coords[1]) + 0.5;
            double y = Double.parseDouble(coords[2]) + 1.0;
            double z = Double.parseDouble(coords[3]) + 0.5;
            player.teleport(new Location(world, x, y, z));
            occupiedSpawnPoints.add(spawnPoint);
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.setExp(0);
            player.setLevel(30);
            player.setHealth(20);
            player.setFoodLevel(20);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                plugin.loadLanguageConfig(onlinePlayer);
                onlinePlayer.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GRAY + plugin.getMessage("setspawnhandler.joined-message-1") + ChatColor.DARK_GREEN + plugin.getMessage("setspawnhandler.joined-message-2") + occupiedSpawnPoints.size() + plugin.getMessage("setspawnhandler.joined-message-3") + spawnPoints.size() + plugin.getMessage("setspawnhandler.joined-message-4"));
            }
            playerSpawnPoints.put(player, spawnPoint);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + plugin.getMessage("setspawnhandler.spawn-filled"));
        }
        return false;
    }

    public void clearOccupiedSpawnPoints() {
        occupiedSpawnPoints.clear();
    }

    public void removeOccupiedSpawnPoint(String spawnPoint) {
        occupiedSpawnPoints.remove(spawnPoint);
    }

    public Map<Player, String> getPlayerSpawnPoints() {
        return playerSpawnPoints;
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.getBlock().getType() == Material.ANVIL || event.getBlock().getType() == Material.CHIPPED_ANVIL || event.getBlock().getType() == Material.DAMAGED_ANVIL) {
            event.setCancelled(true);
        }
    }
}