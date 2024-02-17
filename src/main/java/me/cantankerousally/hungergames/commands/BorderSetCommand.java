package me.cantankerousally.hungergames.commands;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BorderSetCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;

    public BorderSetCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("border")) {
            Player player = (Player) sender;
            if (player.isOp()) {
                if (args.length != 3) {
                    sender.sendMessage("Usage: border <size> <center-x> <center-z>");
                    return true;
                }
                int newSize;
                double centerX, centerZ;
                try {
                    newSize = Integer.parseInt(args[0]);
                    centerX = Double.parseDouble(args[1]);
                    centerZ = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid arguments. Please enter valid numbers.");
                    return true;
                }
                World world = plugin.getServer().getWorld("world");
                if (world != null) {
                    WorldBorder border = world.getWorldBorder();
                    border.setSize(newSize);
                    border.setCenter(centerX, centerZ);
                    sender.sendMessage("World border size set to " + newSize + " and center set to (" + centerX + ", " + centerZ + ")");
                    plugin.reloadConfig();
                    plugin.getConfig().set("border.size", newSize);
                    plugin.getConfig().set("border.center-x", centerX);
                    plugin.getConfig().set("border.center-z", centerZ);
                    plugin.saveConfig();
                } else {
                    sender.sendMessage(ChatColor.RED + "World not found. Please set the level-name in server.properties to world, level-name=world");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            }
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("border")) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                completions.add("<size>");
            } else if (args.length == 2) {
                completions.add("<center-x>");
            } else if (args.length == 3) {
                completions.add("<center-z>");
            }
            return completions;
        }
        return null;
    }
}
