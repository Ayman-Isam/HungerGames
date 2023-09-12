package me.cantankerousally.hungergames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class SetSpawnCommand implements CommandExecutor {

    public SetSpawnCommand() {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender.isOp()) {
            if (command.getName().equalsIgnoreCase("setspawn")) {
                Player player = (Player) sender;
                ItemStack stick = new ItemStack(Material.STICK);
                ItemMeta meta = stick.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.AQUA + "Spawn Point Selector");
                stick.setItemMeta(meta);
                player.getInventory().addItem(stick);
                sender.sendMessage(ChatColor.BLUE + "You have been given a Spawn Point Selector!");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You must be an operator to use this command.");
        }
        return false;
    }
}

