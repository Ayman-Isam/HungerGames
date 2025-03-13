package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.WorldBorderHandler;
import me.aymanisam.hungergames.handlers.WorldResetHandler;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SaveWorldCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final WorldResetHandler worldResetHandler;
    private final WorldBorderHandler worldBorderHandler;

    public SaveWorldCommand(HungerGames plugin, LangHandler langHandler, WorldBorderHandler worldBorderHandler) {
        this.langHandler = langHandler;
        this.worldBorderHandler = worldBorderHandler;
        this.worldResetHandler = new WorldResetHandler(plugin, worldBorderHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!player.hasPermission("hungergames.saveworld")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        World world = player.getWorld();

        worldResetHandler.saveWorldState(world);

        player.sendMessage(langHandler.getMessage(player, "game.worldsaved", world.getName()));

        return true;
    }
}
