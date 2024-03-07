package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
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

    private void sendCountdownMessageToAllPlayers(String messageKey, long delayInTicks) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                plugin.loadLanguageConfig(p);
                String message = plugin.getMessage(messageKey);
                p.sendMessage(ChatColor.GOLD + message);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, delayInTicks);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("start")) {
            if (sender instanceof Player player) {
                plugin.loadLanguageConfig(player);
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
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                    gameStarting = false;
                }, 20L * 20);

                sendCountdownMessageToAllPlayers("startgame.20-s", 20L * 0);
                sendCountdownMessageToAllPlayers("startgame.15-s", 20L * 5);
                sendCountdownMessageToAllPlayers("startgame.10-s", 20L * 10);
                sendCountdownMessageToAllPlayers("startgame.5-s", 20L * 15);
                sendCountdownMessageToAllPlayers("startgame.4-s", 20L * 16);
                sendCountdownMessageToAllPlayers("startgame.3-s", 20L * 17);
                sendCountdownMessageToAllPlayers("startgame.2-s", 20L * 18);
                sendCountdownMessageToAllPlayers("startgame.1-s", 20L * 19);
                return true;
            }
        }
        return false;
    }
}

