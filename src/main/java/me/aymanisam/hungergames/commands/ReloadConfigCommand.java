package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadConfigCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;

    public ReloadConfigCommand(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.configHandler = new ConfigHandler(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            langHandler.getLangConfig(player);
        }

        if (!(sender.hasPermission("hungergames.reloadconfig"))) {
            sender.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        configHandler.checkConfigKeys();
        plugin.reloadConfig();
        sender.sendMessage(langHandler.getMessage("config-reloaded"));
        return true;
    }
}
