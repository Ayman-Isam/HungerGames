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
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;
    private final ArenaHandler arenaHandler;

    public ReloadConfigCommand(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
        this.configHandler = plugin.getConfigHandler();
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!player.hasPermission("hungergames.reloadconfig")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        configHandler.validateConfigKeys(player.getWorld());
        configHandler.loadItemsConfig(player.getWorld());
        configHandler.loadSignLocations();
        configHandler.createWorldConfig(player.getWorld());
        configHandler.createPluginSettings();
        configHandler.validateSettingsKeys();
        langHandler.saveLanguageFiles();
        langHandler.validateLanguageKeys();
        arenaHandler.getArenaConfig(player.getWorld());

        sender.sendMessage(langHandler.getMessage(player, "config-reloaded"));
        return true;
    }
}
