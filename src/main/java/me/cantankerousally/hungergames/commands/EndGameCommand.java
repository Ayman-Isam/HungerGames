package me.cantankerousally.hungergames.commands;

import me.cantankerousally.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EndGameCommand implements CommandExecutor {
    private final HungerGames plugin;

    public EndGameCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player player = (Player) sender;
        if (player.isOp()) {
            if (!plugin.gameStarted) {
                sender.sendMessage(ChatColor.RED + "The game has not started yet!");
                return true;
            }

            plugin.getGameHandler().endGame();
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "The game has ended!");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        }
        return false;
    }
}
