package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReloadConfigCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;
    private final ArenaHandler arenaHandler;

    public ReloadConfigCommand(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.configHandler = new ConfigHandler(plugin);
        this.arenaHandler = new ArenaHandler(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
            return true;
        }

        langHandler.getLangConfig(player);

        if (!player.hasPermission("hungergames.reloadconfig")) {
            player.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        configHandler.checkConfigKeys();
        configHandler.loadItemsConfig(player.getWorld());
        langHandler.saveLanguageFiles();
        langHandler.updateLanguageKeys();
        arenaHandler.getArenaConfig(player.getWorld());

        sender.sendMessage(langHandler.getMessage("config-reloaded"));
        return true;
    }
}
