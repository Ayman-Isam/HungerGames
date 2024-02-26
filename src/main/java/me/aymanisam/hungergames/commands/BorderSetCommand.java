package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BorderSetCommand implements CommandExecutor, TabCompleter {
    private final HungerGames plugin;

    public BorderSetCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("border")) {
            if (sender instanceof Player player) {
                plugin.loadLanguageConfig(player);
                if (player.isOp()) {
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
                        sender.sendMessage(ChatColor.RED + plugin.getMessage("border.invalid-args"));
                        return true;
                    }
                    World world = plugin.getServer().getWorld("world");
                    if (world != null) {
                        WorldBorder border = world.getWorldBorder();
                        border.setSize(newSize);
                        border.setCenter(centerX, centerZ);
                        sender.sendMessage(plugin.getMessage("border.success-message-1") + newSize + plugin.getMessage("border.success-message-2") + centerX +
                                plugin.getMessage("border.success-message-3") + centerZ + plugin.getMessage("border.success-message-4"));
                        plugin.reloadConfig();
                        plugin.getConfig().set("border.size", newSize);
                        plugin.getConfig().set("border.center-x", centerX);
                        plugin.getConfig().set("border.center-z", centerZ);
                        plugin.saveConfig();
                    } else {
                        sender.sendMessage(ChatColor.RED + plugin.getMessage("border.wrong-world"));
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + plugin.getMessage("no-permission"));
                }
            }
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("border")) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                completions.add(plugin.getMessage("border.args-1"));
            } else if (args.length == 2) {
                completions.add(plugin.getMessage("border.args-2"));
            } else if (args.length == 3) {
                completions.add(plugin.getMessage("border.args-3"));
            }
            return completions;
        }
        return null;
    }
}
