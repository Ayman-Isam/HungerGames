package me.cantankerousally.hungergames.commands;

import me.cantankerousally.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class StartGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private boolean gameStarting = false;

    public StartGameCommand(HungerGames plugin) {
        this.plugin = plugin;
        createSetSpawnConfig();
        createArenaConfig();
    }

    private FileConfiguration setspawnConfig = null;

    private FileConfiguration arenaConfig = null;

    public void createSetSpawnConfig() {
        File setspawnFile = new File(plugin.getDataFolder(), "setspawn.yml");
        if (!setspawnFile.exists()) {
            setspawnFile.getParentFile().mkdirs();
            plugin.saveResource("setspawn.yml", false);
        }

        setspawnConfig = YamlConfiguration.loadConfiguration(setspawnFile);
    }

    public void createArenaConfig() {
        File arenaFile = new File(plugin.getDataFolder(), "arena.yml");
        if (!arenaFile.exists()) {
            arenaFile.getParentFile().mkdirs();
            plugin.saveResource("arena.yml", false);
        }

        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("start")) {
            if (sender instanceof Player player) {
                FileConfiguration config = plugin.getConfig();
                if (plugin.getServer().getOnlinePlayers().size() < config.getInt("min-players")) {
                    String message = String.format(plugin.getMessage("startgame.min-players"), config.getInt("min-players"));
                    player.sendMessage(ChatColor.RED + message);
                    return true;
                }
                List<String> spawnpoints = setspawnConfig.getStringList("spawnpoints");
                String world = arenaConfig.getString("region.world");
                if (spawnpoints.isEmpty() && world == null) {
                    player.sendMessage(ChatColor.RED + plugin.getMessage("startgame.set-spawn-arena"));
                    return true;
                } else if (spawnpoints.isEmpty()) {
                    player.sendMessage(ChatColor.RED + plugin.getMessage("startgame.set-spawn"));
                    return true;
                } else if (world == null) {
                    player.sendMessage(ChatColor.RED + plugin.getMessage("startgame.set-arena"));
                    return true;
                }

                if (plugin.gameStarted) {
                    sender.sendMessage(ChatColor.RED + plugin.getMessage("startgame.started"));
                    return true;
                }

                if (gameStarting) {
                    sender.sendMessage(ChatColor.RED + plugin.getMessage("startgame.starting"));
                    return true;
                }

                gameStarting = true;

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    plugin.getGameHandler().startGame();
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                    gameStarting = false;
                }, 20L * 20);

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    plugin.getServer().broadcastMessage(ChatColor.GOLD + plugin.getMessage("startgame.15-s"));
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                    }
                }, 20L * 5);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    plugin.getServer().broadcastMessage(ChatColor.GOLD + plugin.getMessage("startgame.10-s"));
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                    }
                }, 20L * 10);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    plugin.getServer().broadcastMessage(ChatColor.GOLD + plugin.getMessage("startgame.5-s"));
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                    }
                }, 20L * 15);

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    plugin.getServer().broadcastMessage(ChatColor.GOLD + plugin.getMessage("startgame.4-s"));
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                    }
                }, 20L * 16);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    plugin.getServer().broadcastMessage(ChatColor.GOLD + plugin.getMessage("startgame.3-s"));
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                    }
                }, 20L * 17);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    plugin.getServer().broadcastMessage(ChatColor.GOLD + plugin.getMessage("startgame.2-s"));
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                    }
                }, 20L * 18);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    plugin.getServer().broadcastMessage(ChatColor.GOLD + plugin.getMessage("startgame.1-s"));
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                    }
                }, 20L * 19);

                Bukkit.broadcastMessage(ChatColor.GOLD + plugin.getMessage("startgame.20-s"));
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                }
                return true;
            }
        }
        return false;
    }
}

