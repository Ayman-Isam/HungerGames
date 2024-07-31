package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SupplyDropHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.aymanisam.hungergames.HungerGames.gameWorld;

public class SupplyDropCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final SupplyDropHandler supplyDropHandler;


    public SupplyDropCommand(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
        this.supplyDropHandler = new SupplyDropHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        ;

        if (!player.hasPermission("hungergames.supplydrop")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        supplyDropHandler.setSupplyDrop(player.getWorld());

        return true;
    }
}
