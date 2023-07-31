package me.cantankerousally.hungergames.commands;

import me.cantankerousally.hungergames.HungerGames;
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
            sender.sendMessage("You do not have permission to start the game!");
            return true;
        }

        // Check if the game has already started
        if (plugin.gameStarted) {
            sender.sendMessage("The game has already started!");
            return true;
        }

        // Start the game
        plugin.getGameHandler().startGame();
        sender.sendMessage("The game has started!");

        return true;
    }
}

