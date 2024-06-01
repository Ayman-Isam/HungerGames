package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SupplyDropHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SupplyDropCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final SupplyDropHandler supplyDropHandler;


    public SupplyDropCommand(HungerGames plugin) {
        this.langHandler = new LangHandler(plugin);
        this.supplyDropHandler = new SupplyDropHandler(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (!(player.hasPermission("hungergames.supplydrop"))) {
                sender.sendMessage(langHandler.getMessage("no-permission"));
                return true;
            }
            langHandler.getLangConfig(player);
        }

        supplyDropHandler.setSupplyDrop();

        return true;
    }
}
