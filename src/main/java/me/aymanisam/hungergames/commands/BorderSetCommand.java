package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BorderSetCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ArenaHandler arenaHandler;

    public BorderSetCommand(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.arenaHandler = new ArenaHandler(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
            return true;
        }

        langHandler.getLangConfig(player);

        if (!(player.hasPermission("hungergames.border"))) {
            sender.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(langHandler.getMessage("border.usage"));
            return true;
        }

        int newSize, centerX, centerZ;

        try {
            newSize = Integer.parseInt(args[0]);
            centerX = Integer.parseInt(args[1]);
            centerZ = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(langHandler.getMessage("border.invalid-args"));
            return true;
        }

        String worldName = arenaHandler.getArenaConfig().getString("region.world");
        World world = plugin.getServer().getWorld(worldName);

        if (world == null) {
            sender.sendMessage(langHandler.getMessage("border.wrong-world"));
            return true;
        }

        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setSize(newSize);
        worldBorder.setCenter(centerX, centerZ);
        sender.sendMessage(langHandler.getMessage("border.success-message-1") + newSize + langHandler.getMessage("border.success-message-2") + centerX + langHandler.getMessage("border.success-message-3") + centerZ);
        plugin.reloadConfig();
        plugin.getConfig().set("border.size", newSize);
        plugin.getConfig().set("border.center-x", centerX);
        plugin.getConfig().set("border.center-z", centerZ);
        plugin.saveConfig();

        return true;
    }
}
