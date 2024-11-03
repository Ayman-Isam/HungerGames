package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.ChestRefillHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChestRefillCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final ArenaHandler arenaHandler;
    private final ChestRefillHandler chestRefillHandler;

    public ChestRefillCommand(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
        this.chestRefillHandler = new ChestRefillHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!player.hasPermission("hungergames.chestrefill")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        FileConfiguration ArenaConfig = arenaHandler.getArenaConfig(player.getWorld());
        String worldName = ArenaConfig.getString("region.world");

        if (worldName == null) {
            sender.sendMessage(langHandler.getMessage(player, "chestrefill.no-arena"));
            return true;
        }

        chestRefillHandler.refillChests(player.getWorld());

        return true;
    }
}
