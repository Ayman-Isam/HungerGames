package me.aymanisam.hungergames.handler;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.commands.ChestRefillCommand;
import me.aymanisam.hungergames.commands.SupplyDropCommand;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameHandler implements Listener {

    private final HungerGames plugin;
    private final SetSpawnHandler setSpawnHandler;
    private final PlayerSignClickManager playerSignClickManager;
    private FileConfiguration arenaConfig = null;
    private File arenaFile = null;

    public GameHandler(HungerGames plugin, SetSpawnHandler setSpawnHandler, PlayerSignClickManager playerSignClickManager) {
        this.plugin = plugin;
        this.setSpawnHandler = setSpawnHandler;
        this.playersAlive = new ArrayList<>();
        this.playerSignClickManager = playerSignClickManager;
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

    private int timerTaskId;
    private int timeLeft;
    private List<Player> playersAlive;
    private BukkitTask supplyDropTask;
    private BukkitTask chestRefillTask;
    private BukkitTask borderShrinkTask;

    public void startGame() {
        // Start game
        plugin.gameStarted = true;
        WorldBorderHandler worldBorderHandler = new WorldBorderHandler(plugin);
        worldBorderHandler.startBorderShrink();

        // Set the time left
        timeLeft = plugin.getConfig().getInt("game-time");

        // Initialize the list of players alive
        playersAlive = new ArrayList<>();

        // Get the arena region from the config
        FileConfiguration config = getArenaConfig();
        ConfigurationSection regionSection = config.getConfigurationSection("region");
        assert regionSection != null;
        String worldName = regionSection.getString("world");
        assert worldName != null;
        World world = plugin.getServer().getWorld(worldName);
        ConfigurationSection pos1Section = regionSection.getConfigurationSection("pos1");
        assert pos1Section != null;
        double x1 = pos1Section.getDouble("x");
        double y1 = pos1Section.getDouble("y");
        double z1 = pos1Section.getDouble("z");
        ConfigurationSection pos2Section = regionSection.getConfigurationSection("pos2");
        assert pos2Section != null;
        double x2 = pos2Section.getDouble("x");
        double y2 = pos2Section.getDouble("y");
        double z2 = pos2Section.getDouble("z");

        Location minLocation = new Location(world, Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
        Location maxLocation = new Location(world, Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));

        assert world != null;
        for (Player player : world.getPlayers()) {
            Location playerLocation = player.getLocation();
            if (playerLocation.getX() >= minLocation.getX() && playerLocation.getX() <= maxLocation.getX()
                    && playerLocation.getY() >= minLocation.getY() && playerLocation.getY() <= maxLocation.getY()
                    && playerLocation.getZ() >= minLocation.getZ() && playerLocation.getZ() <= maxLocation.getZ()) {
                plugin.bossBar.addPlayer(player);
                playersAlive.add(player);
            }
        }

        for (Player player : playersAlive) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + plugin.getMessage("game.game-start"));
            player.sendMessage(ChatColor.LIGHT_PURPLE + plugin.getMessage("game.grace-start"));
            if (plugin.getConfig().getBoolean("bedrock-buff.enabled") && player.getName().startsWith(".")) {
                List<String> effectNames = plugin.getConfig().getStringList("bedrock-buff.effects");
                for (String effectName : effectNames) {
                    PotionEffectType effectType = PotionEffectType.getByName(effectName);
                    if (effectType != null) {
                        player.addPotionEffect(new PotionEffect(effectType, 200000, 1, true, false));
                    }
                }
            }
        }

        world.setPVP(false);
        int gracePeriod = plugin.getConfig().getInt("grace-period");
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            world.setPVP(true);
            for (Player player : playersAlive) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + plugin.getMessage("game.grace-end"));
            }
        }, gracePeriod * 20L);

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("gameinfo", "dummy", "Game Info", RenderType.INTEGER);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score timeLeftScore = objective.getScore(plugin.getMessage("game.score-time"));
        timeLeftScore.setScore(timeLeft);
        Score playersAliveScore = objective.getScore(plugin.getMessage("game.score-alive"));
        playersAliveScore.setScore(playersAlive.size());
        Score worldBorderSizeScore = objective.getScore(plugin.getMessage("game.score-border"));
        worldBorderSizeScore.setScore((int) world.getWorldBorder().getSize());

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            worldBorderSizeScore.setScore((int) world.getWorldBorder().getSize());
        }, 0L, 20L);

        for (Player player : playersAlive) {
            player.setScoreboard(scoreboard);
        }

        timerTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            plugin.bossBar.setProgress((double) timeLeft / plugin.getConfig().getInt("game-time"));
            timeLeft--;
            timeLeftScore.setScore(timeLeft);

            if (playersAlive.size() == 1) {
                plugin.getServer().getScheduler().cancelTask(timerTaskId);

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + plugin.getMessage("game.game-end"));
                }

                Player winner = playersAlive.get(0);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + winner.getName() + plugin.getMessage("game.winner-text"));
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }
                endGame();
            }

            if (timeLeft < 0) {
                plugin.getServer().getScheduler().cancelTask(timerTaskId);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + plugin.getMessage("game.time-up"));
                }
                endGame();
            }
        }, 0L, 20L);
        int supplyDropInterval = plugin.getConfig().getInt("supplydrop.interval") * 20; // Convert seconds to ticks
        SupplyDropCommand supplyDropCommand = new SupplyDropCommand(plugin);
        PluginCommand supplyDropPluginCommand = plugin.getCommand("supplydrop");

        supplyDropTask = new BukkitRunnable() {
            @Override
            public void run() {
                assert supplyDropPluginCommand != null;
                supplyDropCommand.onCommand(plugin.getServer().getConsoleSender(), supplyDropPluginCommand, "supplydrop", new String[0]);
            }
        }.runTaskTimer(plugin, supplyDropInterval, supplyDropInterval);


        ChestRefillCommand chestRefillCommand = new ChestRefillCommand(plugin);
        PluginCommand chestRefillPluginCommand = plugin.getCommand("chestrefill");
        assert chestRefillPluginCommand != null;
        chestRefillCommand.onCommand(plugin.getServer().getConsoleSender(), chestRefillPluginCommand, "chestrefill", new String[0]);

        // Schedule a delayed task to refill chests again at specified time
        int chestRefillTime = plugin.getConfig().getInt("chestrefill.time") * 20; // Convert seconds to ticks
        chestRefillTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.gameStarted) {
                    chestRefillCommand.onCommand(plugin.getServer().getConsoleSender(), chestRefillPluginCommand, "chestrefill", new String[0]);
                }
            }
        }.runTaskLater(plugin, chestRefillTime);
    }
    public PlayerSignClickManager getPlayerSignClickManager() {
        return playerSignClickManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playersAlive != null) {
            plugin.bossBar.removePlayer(player);
            playerSignClickManager.removePlayerSignClicked(player);
            playersAlive.remove(player);
            World world = plugin.getServer().getWorld("world");
            assert world != null;
            Location spawnLocation = world.getSpawnLocation();
            player.teleport(spawnLocation);
            Map<Player, String> playerSpawnPoints = setSpawnHandler.getPlayerSpawnPoints();
            String spawnPoint = playerSpawnPoints.get(player);
            setSpawnHandler.removeOccupiedSpawnPoint(spawnPoint);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (playersAlive != null) {
            playersAlive.remove(player);
            playerSignClickManager.removePlayerSignClicked(player);
        }
        World world = plugin.getServer().getWorld("world");
        assert world != null;
        Location spawnLocation = world.getSpawnLocation();
        player.teleport(spawnLocation);
        Map<Player, String> playerSpawnPoints = setSpawnHandler.getPlayerSpawnPoints();
        String spawnPoint = playerSpawnPoints.get(player);
        setSpawnHandler.removeOccupiedSpawnPoint(spawnPoint);
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            List<Map<?, ?>> effectMaps = plugin.getConfig().getMapList("killer-effects");
            for (Map<?, ?> effectMap : effectMaps) {
                String effectName = (String) effectMap.get("effect");
                int duration = (int) effectMap.get("duration");
                int level = (int) effectMap.get("level");
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    killer.addPotionEffect(new PotionEffect(effectType, duration, level));
                }
            }
        }
        updatePlayersAliveScore();
        player.setGameMode(GameMode.SPECTATOR);
    }


    public void updatePlayersAliveScore() {
        for (Player player : playersAlive) {
            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getObjective("gameinfo");
            assert objective != null;
            Score playersAliveScore = objective.getScore("Players Alive:");
            playersAliveScore.setScore(playersAlive.size());
        }
    }


    public void endGame() {
        plugin.gameStarted = false;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.setGameMode(GameMode.ADVENTURE);
            playerSignClickManager.removePlayerSignClicked(player);
        }

        World world = plugin.getServer().getWorld("world");
        assert world != null;
        WorldBorder border = world.getWorldBorder();
        double borderSize = plugin.getConfig().getDouble("border.size");
        border.setSize(borderSize);
        Server server = plugin.getServer();

        if (!world.getEntitiesByClass(Item.class).isEmpty()) {
            server.dispatchCommand(server.getConsoleSender(), "kill @e[type=item]");
        }

        if (!world.getEntitiesByClass(ExperienceOrb.class).isEmpty()) {
            server.dispatchCommand(server.getConsoleSender(), "kill @e[type=experience_orb]");
        }

        if (!world.getEntitiesByClass(Arrow.class).isEmpty()) {
            server.dispatchCommand(server.getConsoleSender(), "kill @e[type=arrow]");
        }

        if (!world.getEntitiesByClass(Trident.class).isEmpty()) {
            server.dispatchCommand(server.getConsoleSender(), "kill @e[type=trident]");
        }

        plugin.getServer().getScheduler().cancelTask(timerTaskId);

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard emptyScoreboard = manager.getNewScoreboard();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.bossBar.removePlayer(player);
            player.setScoreboard(emptyScoreboard);
        }
        playersAlive.clear();

        setSpawnHandler.clearOccupiedSpawnPoints();

        Location spawnLocation = world.getSpawnLocation();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.teleport(spawnLocation);
            player.getInventory().clear();
        }
        world.setPVP(false);

        if (supplyDropTask != null) {
            supplyDropTask.cancel();
            supplyDropTask = null;
        }

        if (chestRefillTask != null) {
            chestRefillTask.cancel();
            chestRefillTask = null;
        }

        if (borderShrinkTask != null) {
            borderShrinkTask.cancel();
            borderShrinkTask = null;
        }

        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState blockState : chunk.getTileEntities()) {
                if (blockState instanceof ShulkerBox shulkerBox) {
                    if (shulkerBox.getColor() == DyeColor.RED) {
                        shulkerBox.getBlock().setType(Material.AIR);
                    }
                }
            }
        }
    }
}