package me.cantankerousally.hungergames.commands;

import me.cantankerousally.hungergames.HungerGames;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartGameCommand implements CommandExecutor {
    private HungerGames plugin;

    public StartGameCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender has permission to start the game
        if (!sender.hasPermission("hungergames.start")) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE  + "You do not have permission to start the game!");
            return true;
        }

        // Check if the game has already started
        if (plugin.gameStarted) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE  + "The game has already started!");
            return true;
        }

        // Schedule a delayed task to start the game after 20 seconds
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                // Start the game
                plugin.getGameHandler().startGame();
                sender.sendMessage(ChatColor.LIGHT_PURPLE  + "The game has started!");
            }
        }, 20L * 20);

        // Schedule additional delayed tasks to send countdown messages
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                sender.sendMessage(ChatColor.LIGHT_PURPLE  + "15 seconds left!");
            }
        }, 20L * 5);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                sender.sendMessage(ChatColor.LIGHT_PURPLE  + "10 seconds left!");
            }
        }, 20L * 10);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                sender.sendMessage(ChatColor.LIGHT_PURPLE  + "5 seconds left!");
            }
        }, 20L * 15);

        // Send a message to the sender
        sender.sendMessage(ChatColor.LIGHT_PURPLE  + "The game will start in 20 seconds!");

        return true;
    }
}
