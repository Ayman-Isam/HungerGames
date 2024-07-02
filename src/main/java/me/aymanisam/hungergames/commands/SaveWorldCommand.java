package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.WorldResetHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SaveWorldCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final WorldResetHandler worldResetHandler;

    public SaveWorldCommand(HungerGames plugin) {
        this.langHandler = new LangHandler(plugin);
        this.worldResetHandler = new WorldResetHandler(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
            return true;
        }

        langHandler.getLangConfig(player);

        if (!player.hasPermission("hungergames.saveworld")) {
            player.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        worldResetHandler.saveWorldState(player.getWorld());

        player.sendMessage(langHandler.getMessage("game.worldsaved", player.getWorld().getName()));

        return true;
    }
}
