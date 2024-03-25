package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("end")) {
            if (sender instanceof Player player) {
                plugin.loadLanguageConfig(player);
                if (player.hasPermission("hungergames.end")) {
                    if (!plugin.gameStarted) {
                        sender.sendMessage(plugin.getMessage("endgame.not-started"));
                        return true;
                    }

                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        plugin.loadLanguageConfig(p);
                        p.sendMessage(plugin.getMessage("endgame.ended"));
                    }
                    plugin.getGameHandler().endGame();

                    return true;
                } else {
                    sender.sendMessage(plugin.getMessage("no-permission"));
                }
                return false;
            }
            return true;
        }
        return true;
    }
}
