package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.*;

public class ArenaInitializeCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;

    public ArenaInitializeCommand(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!player.hasPermission("hungergames.border")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (!(args.length == 1)) {
            sender.sendMessage(langHandler.getMessage(player, "map.no-args"));
            return true;
        }

        String worldName = args[0];

        if (!worldNames.contains(worldName)) {
            sender.sendMessage(langHandler.getMessage(player, "map.not-found", worldName));
            plugin.getLogger().info("Loaded maps:" + plugin.getServer().getWorlds().stream().map(World::getName).collect(Collectors.joining(", ")));
            return true;
        }

        worldCreated.put(worldName, true);

        int pin = new SecureRandom().nextInt(100000);

        player.sendMessage(langHandler.getMessage(player,"game.initialized", worldName, pin));

        worldPins.put(worldName, pin);

        return true;
    }
}
