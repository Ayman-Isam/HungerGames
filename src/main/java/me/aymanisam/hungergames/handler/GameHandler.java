package me.aymanisam.hungergames.handler;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.commands.ChestRefillCommand;
import me.aymanisam.hungergames.commands.JoinGameCommand;
import me.aymanisam.hungergames.commands.SupplyDropCommand;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class GameHandler implements Listener {

    private final HungerGames plugin;
    private final SetSpawnHandler setSpawnHandler;
    private final PlayerSignClickManager playerSignClickManager;
    private final JoinGameCommand joinGameCommand;
    private final WorldBorderHandler worldBorderHandler;
    private FileConfiguration arenaConfig = null;

    public GameHandler(HungerGames plugin, SetSpawnHandler setSpawnHandler, PlayerSignClickManager playerSignClickManager, JoinGameCommand joinGameCommand) {
        this.plugin = plugin;
        this.setSpawnHandler = setSpawnHandler;
        this.joinGameCommand = joinGameCommand;
        this.playersAlive = new ArrayList<>();
        this.playerSignClickManager = playerSignClickManager;
        this.worldBorderHandler = new WorldBorderHandler(plugin);
        createArenaConfig();
    }

    public void createArenaConfig() {
        File arenaFile = new File(plugin.getDataFolder(), "arena.yml");
        if (!arenaFile.exists()) {
            boolean dirsCreated = arenaFile.getParentFile().mkdirs();
            if (!dirsCreated) {
                plugin.getLogger().log(Level.SEVERE, "Could not create necessary directories.");
                return;
            }
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

    public List<Player> getPlayersAlive() {
        return playersAlive;
    }

    public void startGame() {

        // Start game
        plugin.gameStarted = true;

        // Set the time left
        timeLeft = plugin.getConfig().getInt("game-time");

        // Initialize the list of players alive
        playersAlive = new ArrayList<>();

        worldBorderHandler.startBorderShrink();

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

        Map<Player, BossBar> playerBossBars = new HashMap<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Location playerLocation = player.getLocation();
            if (playerLocation.getX() >= minLocation.getX() && playerLocation.getX() <= maxLocation.getX()
                    && playerLocation.getY() >= minLocation.getY() && playerLocation.getY() <= maxLocation.getY()
                    && playerLocation.getZ() >= minLocation.getZ() && playerLocation.getZ() <= maxLocation.getZ()) {
                plugin.loadLanguageConfig(player);
                BossBar bossBar = plugin.getServer().createBossBar(plugin.getMessage("time-remaining"), BarColor.GREEN, BarStyle.SOLID);
                bossBar.addPlayer(player);

                playerBossBars.put(player, bossBar);

                plugin.setBossBar(bossBar);
                playersAlive.add(player);
                player.sendMessage(plugin.getMessage("game.game-start"));
                player.sendMessage(plugin.getMessage("game.grace-start"));
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
        }

        plugin.setPlayerBossBars(playerBossBars);

        int gracePeriod = plugin.getConfig().getInt("grace-period");
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            world.setPVP(true);
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                plugin.loadLanguageConfig(player);
                player.sendMessage(plugin.getMessage("game.grace-end"));
            }
        }, gracePeriod * 20L);

        timerTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<Player, BossBar> entry : playerBossBars.entrySet()) {
                BossBar bossBar = entry.getValue();
                bossBar.setProgress((double) timeLeft / plugin.getConfig().getInt("game-time"));

                // Format timeLeft into minutes and seconds
                int minutes = (timeLeft - 1) / 60;
                int seconds = (timeLeft - 1) % 60;
                String timeFormatted = String.format("%02d:%02d", minutes, seconds);

                // Set the formatted time as the BossBar title
                bossBar.setTitle(plugin.getMessage("game.score-time") + timeFormatted);
            }
            timeLeft--;

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                plugin.loadLanguageConfig(player);
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                assert manager != null;
                Scoreboard scoreboard = manager.getNewScoreboard();

                Objective objective = scoreboard.registerNewObjective(plugin.getMessage("game.score-name"), "dummy", plugin.getMessage("game.score-name"), RenderType.INTEGER);
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);

                objective.getScore("  ").setScore(15);

                Score playersAliveScore = objective.getScore(plugin.getMessage("game.score-alive") + ChatColor.GREEN + playersAlive.size());
                playersAliveScore.setScore(14);

                Score worldBorderSizeScore = objective.getScore(plugin.getMessage("game.score-border") + ChatColor.GREEN + (int) world.getWorldBorder().getSize());
                worldBorderSizeScore.setScore(13);

                objective.getScore("  ").setScore(12);

                String timeFormatted = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
                Score timeScore = objective.getScore(plugin.getMessage("game.score-time") + ChatColor.GREEN + timeFormatted);
                timeScore.setScore(11);

                int borderShrinkTimeConfig = plugin.getConfig().getInt("border.start-time");
                int gameTimeConfig = plugin.getConfig().getInt("game-time");
                int borderShrinkTimeLeft = (timeLeft - gameTimeConfig) + borderShrinkTimeConfig;
                int borderMinutes = borderShrinkTimeLeft / 60;
                int borderSeconds = borderShrinkTimeLeft % 60;
                String borderTimeFormatted = String.format("%02d:%02d", borderMinutes, borderSeconds);
                Score borderShrinkScore = objective.getScore(plugin.getMessage("game.score-borderShrink") + ChatColor.GREEN + borderTimeFormatted);
                if (borderShrinkTimeLeft >= 0) {
                    borderShrinkScore.setScore(10);
                }

                int pvpTimeConfig = plugin.getConfig().getInt("grace-period");
                int pvpTimeLeft = (timeLeft - gameTimeConfig) + pvpTimeConfig;
                int pvpMinutes = pvpTimeLeft / 60;
                int pvpSeconds = pvpTimeLeft % 60;
                String pvpTimeFormatted = String.format("%02d:%02d", pvpMinutes, pvpSeconds);
                Score pvpScore = objective.getScore(plugin.getMessage("game.score-pvp") + ChatColor.GREEN + pvpTimeFormatted);
                if (pvpTimeLeft >= 0) {
                    pvpScore.setScore(9);
                }

                objective.getScore("").setScore(8);

                int chestRefillInterval = plugin.getConfig().getInt("chestrefill.interval");
                int chestRefillTimeLeft = timeLeft % chestRefillInterval;
                int chestMinutes = chestRefillTimeLeft / 60;
                int chestSeconds = chestRefillTimeLeft % 60;
                String chestTimeFormatted = String.format("%02d:%02d", chestMinutes, chestSeconds);
                Score chestRefillScore = objective.getScore(plugin.getMessage("game.score-chestrefill") + ChatColor.GREEN + chestTimeFormatted);
                if (chestRefillTimeLeft >= 0) {
                    chestRefillScore.setScore(7);
                }

                int supplyDropInterval = plugin.getConfig().getInt("supplydrop.interval");
                int supplyDropTimeLeft = timeLeft % supplyDropInterval;
                Score supplyDropScore = objective.getScore(plugin.getMessage("game.score-supplydrop") + ChatColor.GREEN + supplyDropTimeLeft);
                if (supplyDropTimeLeft >= 0) {
                    supplyDropScore.setScore(6);
                }

                if (plugin.getConfig().getBoolean("show-supplydrop")) {
                    List<String> supplyDropCoords = SupplyDropCommand.coords;
                    int maxDisplay = Math.min(supplyDropCoords.size(), plugin.getConfig().getInt("num-supply-drops"));
                    for (int i = 0; i < maxDisplay; i++) {
                        String supplyDropCoord = supplyDropCoords.get(supplyDropCoords.size() - 1 - i);
                        Score supplyDropCoordsScore = objective.getScore(plugin.getMessage("game.score-supplydrop-latest") + ChatColor.GREEN + supplyDropCoord);
                        supplyDropCoordsScore.setScore(maxDisplay - i);
                    }
                }
                player.setScoreboard(scoreboard);
            }

            if (playersAlive.size() == 1) {
                plugin.getServer().getScheduler().cancelTask(timerTaskId);

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    plugin.loadLanguageConfig(player);
                    player.sendMessage(plugin.getMessage("game.game-end"));
                }

                Player winner = playersAlive.get(0);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    plugin.loadLanguageConfig(player);
                    player.sendMessage(ChatColor.LIGHT_PURPLE + winner.getName() + plugin.getMessage("game.winner-text"));
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }
                endGame();
            }

            if (timeLeft < 0) {
                plugin.getServer().getScheduler().cancelTask(timerTaskId);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    plugin.loadLanguageConfig(player);
                    player.sendMessage(plugin.getMessage("game.time-up"));
                }
                endGame();
            }
        }, 0L, 20L);
        int supplyDropInterval = plugin.getConfig().getInt("supplydrop.interval") * 20;
        SupplyDropCommand supplyDropCommand = new SupplyDropCommand(plugin);
        PluginCommand supplyDropPluginCommand = plugin.getCommand("supplydrop");

        supplyDropTask = new BukkitRunnable() {
            @Override
            public void run() {
                assert supplyDropPluginCommand != null;
                supplyDropCommand.onCommand(plugin.getServer().getConsoleSender(), supplyDropPluginCommand, "supplydrop", new String[0]);
            }
        }.runTaskTimer(plugin, supplyDropInterval, supplyDropInterval);

        int chestRefillInterval = plugin.getConfig().getInt("chestrefill.interval") * 20;
        ChestRefillCommand chestRefillCommand = new ChestRefillCommand(plugin);
        PluginCommand chestRefillPluginCommand = plugin.getCommand("chestrefill");

        chestRefillTask = new BukkitRunnable() {
            @Override
            public void run() {
                chestRefillCommand.onCommand(plugin.getServer().getConsoleSender(), chestRefillPluginCommand, "chestrefill", new String[0]);
            }
        }.runTaskTimer(plugin, 0, chestRefillInterval);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playersAlive != null) {
            if (plugin.bossBar != null) {
                plugin.bossBar.removePlayer(player);
            }
            playerSignClickManager.removePlayerSignClicked(player);
            playersAlive.remove(player);
            joinGameCommand.removePlayer(player);
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
        plugin.loadLanguageConfig(player);
        boolean spectating = plugin.getConfig().getBoolean("spectating");
        if (spectating) {
            player.setGameMode(GameMode.SPECTATOR);
        }
        boolean autoJoin = plugin.getConfig().getBoolean("auto-join");
        if (autoJoin) {
            int numSpawnPoints = setSpawnHandler.getNumSpawnPoints();
            int numPlayersInGame = joinGameCommand.getPlayersInGame().size();
            if (numPlayersInGame >= numSpawnPoints) {
                player.sendMessage(plugin.getMessage("join.join-fail"));
                return;
            }

            event.setJoinMessage(null);
            setSpawnHandler.handleJoin(player);
            joinGameCommand.addPlayerToGame(player);
            player.sendMessage(plugin.getMessage("join.auto-join"));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.gameStarted) {
            return;
        }

        Player player = event.getEntity();
        if (playersAlive != null) {
            playersAlive.remove(player);
            playerSignClickManager.removePlayerSignClicked(player);
            joinGameCommand.removePlayer(player);
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
        boolean spectating = plugin.getConfig().getBoolean("spectating");
        if (spectating) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(plugin.getMessage("spectate.message"));
        }

        Map<Player, BossBar> playerBossBars = plugin.getPlayerBossBars();
        BossBar bossBar = playerBossBars.get(player);
        if (bossBar != null) {
            plugin.bossBar.removePlayer(player);
        }
        Location location = player.getLocation();

        world.spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 10);
        world.spawnParticle(Particle.REDSTONE, location, 50, new Particle.DustOptions(Color.RED, 1.0f));
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.4f, 1.0f);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            plugin.loadLanguageConfig(p);
            if (killer != null)
                p.sendMessage(player.getName() + plugin.getMessage("game.killed-message") + killer.getName());
            else {
                p.sendMessage(player.getName() + plugin.getMessage("game.death-message"));
            }
        }
        if (plugin.gameStarted) {
            event.setDeathMessage(null);
        }
    }


    public void updatePlayersAliveScore() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.loadLanguageConfig(player);
            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getObjective("gameinfo");
            if (objective == null) {
                objective = scoreboard.registerNewObjective("gameinfo", "dummy", "Game Info", RenderType.INTEGER);
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            }
            Score playersAliveScore = objective.getScore("Players Alive:");
            playersAliveScore.setScore(playersAlive.size());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        World world = plugin.getServer().getWorld("world");
        assert world != null;
        Location spawnLocation = world.getSpawnLocation();
        event.setRespawnLocation(spawnLocation);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            if (event.getClickedBlock() != null) {
                Material blockType = event.getClickedBlock().getType();
                if (blockType == Material.CHEST || blockType == Material.TRAPPED_CHEST || blockType == Material.BARREL || blockType == Material.RED_SHULKER_BOX) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public void endGame() {
        if (!plugin.gameStarted) {
            return;
        }

        plugin.gameStarted = false;
        World world = plugin.getServer().getWorld("world");
        assert world != null;
        Location spawnLocation = world.getSpawnLocation();

        Map<Player, BossBar> playerBossBars = plugin.getPlayerBossBars();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            BossBar bossBar = playerBossBars.get(player);

            if (bossBar != null) {
                bossBar.removePlayer(player);
            }
            player.setGameMode(GameMode.ADVENTURE);
            playerSignClickManager.removePlayerSignClicked(player);
            joinGameCommand.removePlayer(player);
            player.teleport(spawnLocation);
            player.getInventory().clear();
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            assert manager != null;
            Scoreboard emptyScoreboard = manager.getNewScoreboard();
            player.setScoreboard(emptyScoreboard);
        }

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
            player.setScoreboard(emptyScoreboard);
        }
        playersAlive.clear();

        setSpawnHandler.clearOccupiedSpawnPoints();

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
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (plugin.bossBar != null) {
                plugin.bossBar.removePlayer(player);
            }
        }
    }
}