package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCreateCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandlerInstance;

    public ArenaCreateCommand(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandlerInstance = new LangHandler(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandlerInstance.getMessage("no-server"));
            return true;
        }

        langHandlerInstance.loadLanguageConfig(player);

        if (!(player.hasPermission("hungergames.create"))) {
            sender.sendMessage(langHandlerInstance.getMessage("no-permission"));
            return true;
        }

        if (!(player.hasMetadata("arena_pos1") && player.hasMetadata("arena_pos2"))) {
            sender.sendMessage(langHandlerInstance.getMessage("arena.no-values"));
            return true;
        }

        Location pos1 = (Location) player.getMetadata("arena_pos1").get(0).value();
        Location pos2 = (Location) player.getMetadata("arena_pos2").get(0).value();

        if (pos1 == null || pos2 == null) {
            sender.sendMessage(langHandlerInstance.getMessage("arena.no-values"));
            return true;
        }

        return true;
    }
}
