package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BorderSetCommand implements CommandExecutor {
    private final HungerGames plugin;

    public BorderSetCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            plugin.loadLanguageConfig(player);
            if (player.hasPermission("hungergames.border")) {
                if (args.length != 3) {
                    sender.sendMessage(plugin.getMessage("border.usage"));
                    return true;
                }
                int newSize;
                double centerX, centerZ;
                try {
                    newSize = Integer.parseInt(args[0]);
                    centerX = Double.parseDouble(args[1]);
                    centerZ = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getMessage("border.invalid-args"));
                    return true;
                }
                World world = plugin.getServer().getWorld("world");
                if (world != null) {
                    WorldBorder border = world.getWorldBorder();
                    border.setSize(newSize);
                    border.setCenter(centerX, centerZ);
                    sender.sendMessage(plugin.getMessage("border.success-message-1") + newSize + plugin.getMessage("border.success-message-2") + centerX + plugin.getMessage("border.success-message-3") + centerZ);
                    plugin.reloadConfig();
                    plugin.getConfig().set("border.size", newSize);
                    plugin.getConfig().set("border.center-x", centerX);
                    plugin.getConfig().set("border.center-z", centerZ);
                    plugin.saveConfig();
                } else {
                    sender.sendMessage(plugin.getMessage("border.wrong-world"));
                }
            } else {
                sender.sendMessage(plugin.getMessage("no-permission"));
            }
        }
        return true;
    }
}
