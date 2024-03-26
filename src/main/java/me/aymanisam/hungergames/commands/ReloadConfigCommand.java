package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

public class ReloadConfigCommand implements CommandExecutor {
    private final HungerGames plugin;

    public ReloadConfigCommand(HungerGames plugin) {
        this.plugin = plugin;
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("reloadconfig")) {
            if (sender.hasPermission("hungergames.reloadconfig")) {
                plugin.loadDefaultLanguageConfig();
                plugin.reloadConfig();
                plugin.reloadItemsConfig();
                plugin.checkConfigKeys();
                for (Player player : getServer().getOnlinePlayers()) {
                    plugin.loadLanguageConfig(player);
                }
                sender.sendMessage(plugin.getMessage("config-reloaded"));
            } else {
                sender.sendMessage(plugin.getMessage("no-permission"));
            }
            return true;
        }
        return false;
    }
}
