package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BorderSetCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final ArenaHandler arenaHandler;
    private final ConfigHandler configHandler;

    public BorderSetCommand(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!player.hasPermission("hungergames.border")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(langHandler.getMessage(player, "border.usage"));
            return true;
        }

        World world = player.getWorld();

        if (arenaHandler.getArenaConfig(world).get("region") == null) {
            sender.sendMessage(langHandler.getMessage(player, "supplydrop.no-arena"));
            return true;
        }

        int newSize, centerX, centerZ;

        try {
            newSize = Integer.parseInt(args[0]);
            centerX = Integer.parseInt(args[1]);
            centerZ = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(langHandler.getMessage(player, "border.invalid-args"));
            return true;
        }

        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setSize(newSize);
        worldBorder.setCenter(centerX, centerZ);
        sender.sendMessage(langHandler.getMessage(player, "border.success-message", newSize, centerX, centerZ));

        configHandler.getWorldConfig(world).set("border.size", newSize);
        configHandler.getWorldConfig(world).set("border.center-x", centerX);
        configHandler.getWorldConfig(world).set("border.center-z", centerZ);
        configHandler.saveWorldConfig(world);

        return true;
    }
}
