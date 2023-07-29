package me.cantankerousally.hungergames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArenaSelectorCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("select")) {
            Player player = (Player) sender;
            ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
            ItemMeta meta = blazeRod.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + "Arena Selector");
            blazeRod.setItemMeta(meta);
            player.getInventory().addItem(blazeRod);
            sender.sendMessage(org.bukkit.ChatColor.BLUE + "You have been given an Arena Selector!");
            return true;
        } else if (command.getName().equalsIgnoreCase("create")) {
            Player player = (Player) sender;
            if (player.hasMetadata("arena_pos1") && player.hasMetadata("arena_pos2")) {
                Location pos1 = (Location) player.getMetadata("arena_pos1").get(0).value();
                Location pos2 = (Location) player.getMetadata("arena_pos2").get(0).value();
                // Create a region from pos1 and pos2
                // ...
                sender.sendMessage(ChatColor.GREEN + "Region created!");
            } else {
                sender.sendMessage(ChatColor.RED + "You must set both positions first using the Arena Selector!");
            }
            return true;
        }
        return false;
    }
}
