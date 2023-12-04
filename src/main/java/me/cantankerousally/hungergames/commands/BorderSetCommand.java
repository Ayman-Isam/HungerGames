package me.cantankerousally.hungergames.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BorderSetCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;

    public BorderSetCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("border")) {
            if (args.length != 1) {
                sender.sendMessage("Usage: border <size>");
                return true;
            }
            int newSize;
            try {
                newSize = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid border size. Please enter a number.");
                return true;
            }
            World world = plugin.getServer().getWorld("world");
            if (world != null) {
                world.getWorldBorder().setSize(newSize);
                sender.sendMessage("World border size set to " + newSize);
                plugin.getConfig().set("border-size", newSize);
                plugin.saveConfig();
            } else {
                sender.sendMessage("World not found.");
            }
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("border")) {
            if (args.length == 1) {
                return new ArrayList<>();
            }
        }
        return null;
    }
}
