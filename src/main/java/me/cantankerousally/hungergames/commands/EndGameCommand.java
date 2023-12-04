package me.cantankerousally.hungergames.commands;

import me.cantankerousally.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class EndGameCommand implements CommandExecutor {
    private final HungerGames plugin;

    public EndGameCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!plugin.gameStarted) {
            sender.sendMessage("The game has already ended!");
            return true;
        }

        plugin.getGameHandler().endGame();
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "The game has ended!");
        return true;
    }
}
