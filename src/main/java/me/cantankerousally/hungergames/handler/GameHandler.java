package me.cantankerousally.hungergames.handler;

import me.cantankerousally.hungergames.HungerGames;
import me.cantankerousally.hungergames.commands.ChestRefillCommand;
import me.cantankerousally.hungergames.commands.SupplyDropCommand;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameHandler implements Listener {

    private final HungerGames plugin;
    private final SetSpawnHandler setSpawnHandler;

    public GameHandler(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.setSpawnHandler = setSpawnHandler;
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
        ConfigurationSection regionSection = plugin.getConfig().getConfigurationSection("region");
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

        // Create a cuboid selection for the arena region
        Location minLocation = new Location(world, Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
        Location maxLocation = new Location(world, Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));

        // Add players inside the arena to boss bar and list of players alive
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

        // Send message to players
        for (Player player : playersAlive) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "The game has started!");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "The grace period has started! PvP is disabled!");
            if (player.getName().startsWith(".")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200000, 1, true, false));
            }
        }

        // Turn off PvP
        world.setPVP(false);
        // Schedule a delayed task to turn PvP back on
        int gracePeriod = plugin.getConfig().getInt("grace-period");
        // Turn PvP back on
        // Send message to players
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            // Turn PvP back on
            world.setPVP(true);

            // Send message to players
            for (Player player : playersAlive) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "The grace period has ended! PvP is now enabled!");
            }
        }, gracePeriod * 20L);

        // Create a scoreboard to display the timer and number of players alive
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("gameinfo", "dummy", "Game Info", RenderType.INTEGER);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score timeLeftScore = objective.getScore("Time Left:");
        timeLeftScore.setScore(timeLeft);
        Score playersAliveScore = objective.getScore("Players Alive:");
        playersAliveScore.setScore(playersAlive.size());
        Score worldBorderSizeScore = objective.getScore("World Border Size:");
        worldBorderSizeScore.setScore((int) world.getWorldBorder().getSize());

        // Schedule a repeating task to update the world border size score
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            // Update the world border size score
            worldBorderSizeScore.setScore((int) world.getWorldBorder().getSize());
        }, 0L, 20L);

        // Set the scoreboard for all players
        for (Player player : playersAlive) {
            player.setScoreboard(scoreboard);
        }

        // Schedule a repeating task to update the boss bar's progress
        timerTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            // Update the boss bar's progress
            plugin.bossBar.setProgress((double) timeLeft / plugin.getConfig().getInt("game-time"));

            // Decrement the time left
            timeLeft--;
            timeLeftScore.setScore(timeLeft);

            // Check if there is only one player alive
            if (playersAlive.size() == 1) {
                // Cancel the task
                plugin.getServer().getScheduler().cancelTask(timerTaskId);

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "The game has ended!");
                }


                // Declare the winner
                Player winner = playersAlive.get(0);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + winner.getName() + " is the winner!");
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }

                // End the game
                endGame();
            }

            // Check if the time is up
            if (timeLeft < 0) {
                // Cancel the task
                plugin.getServer().getScheduler().cancelTask(timerTaskId);
                // Send message to players
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "The time is up! No one won the game!");
                }
                // End the game
                endGame();
            }
        }, 0L, 20L);
        // Schedule supply drops
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playersAlive != null) {
            // Remove player from boss bar and list of players alive
            plugin.bossBar.removePlayer(player);
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (playersAlive != null) {
            // Remove player from list of players alive
            playersAlive.remove(player);
        }
        World world = plugin.getServer().getWorld("world");
        assert world != null;
        Location spawnLocation = world.getSpawnLocation();
        player.teleport(spawnLocation);
        Map<Player, String> playerSpawnPoints = setSpawnHandler.getPlayerSpawnPoints();
        String spawnPoint = playerSpawnPoints.get(player);
        setSpawnHandler.removeOccupiedSpawnPoint(spawnPoint);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 4));
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 0));
            killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 300, 0));
            killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 0));
        }
        updatePlayersAliveScore();
    }


    public void updatePlayersAliveScore() {
        // Update the playersAliveScore for each player
        for (Player player : playersAlive) {
            // Get the player's scoreboard
            Scoreboard scoreboard = player.getScoreboard();

            // Get the playersAliveScore
            Objective objective = scoreboard.getObjective("gameinfo");
            assert objective != null;
            Score playersAliveScore = objective.getScore("Players Alive:");

            // Update the playersAliveScore
            playersAliveScore.setScore(playersAlive.size());
        }
    }


    public void endGame() {
        // End game
        plugin.gameStarted = false;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.setGameMode(GameMode.ADVENTURE);
        }

        World world = plugin.getServer().getWorld("world");
        assert world != null;
        WorldBorder border = world.getWorldBorder();
        double borderSize = plugin.getConfig().getDouble("border.size");
        border.setSize(borderSize);

        // Get the server
        Server server = plugin.getServer();

        if (!world.getEntitiesByClass(Item.class).isEmpty()) {
            // Execute the /kill command to remove all item entities
            server.dispatchCommand(server.getConsoleSender(), "kill @e[type=item]");
        }

        // Check if there are any experience orb entities
        if (!world.getEntitiesByClass(ExperienceOrb.class).isEmpty()) {
            // Execute the /kill command to remove all experience orb entities
            server.dispatchCommand(server.getConsoleSender(), "kill @e[type=experience_orb]");
        }

        // Check if there are any arrow entities
        if (!world.getEntitiesByClass(Arrow.class).isEmpty()) {
            // Execute the /kill command to remove all experience orb entities
            server.dispatchCommand(server.getConsoleSender(), "kill @e[type=arrow]");
        }

        plugin.getServer().getScheduler().cancelTask(timerTaskId);

        // Remove players from boss bar and clear list of players alive
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard emptyScoreboard = manager.getNewScoreboard();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.bossBar.removePlayer(player);
            player.setScoreboard(emptyScoreboard);
        }
        playersAlive.clear();

        setSpawnHandler.clearOccupiedSpawnPoints();

        // Teleport players to world spawn location
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

        // Remove all red shulker boxes in the world
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