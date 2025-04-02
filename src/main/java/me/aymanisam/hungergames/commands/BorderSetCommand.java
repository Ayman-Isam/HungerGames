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

import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.hgWorldNames;

public class BorderSetCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ArenaHandler arenaHandler;
    private final ConfigHandler configHandler;

    public BorderSetCommand(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
        this.configHandler = plugin.getConfigHandler();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player player = null;

        if (sender instanceof Player) {
            player = ((Player) sender);
        }

        if (player != null && !player.hasPermission("hungergames.border")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (player != null && args.length != 3) {
            sender.sendMessage(langHandler.getMessage(player, "border.usage"));
            return true;
        }

        World world;

        if (player == null) {
            if (args.length != 1) {
                sender.sendMessage(langHandler.getMessage(null, "no-world"));
                return true;
            }
            String worldName = args[0];
            if (!hgWorldNames.contains(worldName)) {
                sender.sendMessage(langHandler.getMessage(null, "teleport.invalid-world", args[0]));
                plugin.getLogger().info("Loaded maps:" + plugin.getServer().getWorlds().stream().map(World::getName).collect(Collectors.joining(", ")));
                return true;
            }
            world = plugin.getServer().getWorld(worldName);
        } else {
            world = player.getWorld();
        }

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
