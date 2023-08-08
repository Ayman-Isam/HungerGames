package me.cantankerousally.hungergames.commands;

import me.cantankerousally.hungergames.HungerGames;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndGameCommand implements CommandExecutor {
    private HungerGames plugin;

    public EndGameCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender has permission to end the game
        if (!sender.hasPermission("hungergames.end")) {
            sender.sendMessage("You do not have permission to end the game!");
            return true;
        }

        // Check if the game has already ended
        if (!plugin.gameStarted) {
            sender.sendMessage("The game has already ended!");
            return true;
        }



        // End the game
        plugin.getGameHandler().endGame();
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "The game has ended!");

        return true;
    }
}
