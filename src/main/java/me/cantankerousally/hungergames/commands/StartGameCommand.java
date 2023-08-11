package me.cantankerousally.hungergames.commands;

import me.cantankerousally.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StartGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private boolean gameStarting = false;

    public StartGameCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Check if the sender has permission to start the game
        if (!sender.hasPermission("hungergames.start")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to start the game!");
            return true;
        }

        // Check if the game has already started
        if (plugin.gameStarted) {
            sender.sendMessage(ChatColor.RED + "The game has already started!");
            return true;
        }

        // Check if a game start countdown is already in progress
        if (gameStarting) {
            sender.sendMessage(ChatColor.RED + "The game is already starting!");
            return true;
        }

        // Set the gameStarting flag to true
        gameStarting = true;

        // Schedule a delayed task to start the game after 20 seconds
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            // Start the game
            plugin.getGameHandler().startGame();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
            // Set the gameStarting flag back to false
            gameStarting = false;
        }, 20L * 20);

        // Schedule additional delayed tasks to send countdown messages
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.getServer().broadcastMessage(ChatColor.GOLD + "15 seconds left!");
            // Play a note block sound for all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, 20L * 5);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.getServer().broadcastMessage(ChatColor.GOLD + "10 seconds left!");
            // Play a note block sound for all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, 20L * 10);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.getServer().broadcastMessage(ChatColor.GOLD + "5 seconds left!");
            // Play a note block sound for all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, 20L * 15);

        // Schedule additional delayed tasks to send countdown messages
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.getServer().broadcastMessage(ChatColor.GOLD + "4 seconds left!");
            // Play a note block sound for all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, 20L * 16);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.getServer().broadcastMessage(ChatColor.GOLD + "3 seconds left!");
            // Play a note block sound for all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, 20L * 17);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.getServer().broadcastMessage(ChatColor.GOLD + "2 seconds left!");
            // Play a note block sound for all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, 20L * 18);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.getServer().broadcastMessage(ChatColor.GOLD + "1 second left!");
            // Play a note block sound for all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, 20L * 19);

        // Send a message to the sender
        Bukkit.broadcastMessage(ChatColor.GOLD + "The game will start in 20 seconds!");
        // Play a note block sound for all online players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        }
        return true;
    }
}

