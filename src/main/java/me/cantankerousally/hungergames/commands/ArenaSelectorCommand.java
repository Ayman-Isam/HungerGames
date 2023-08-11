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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ArenaSelectorCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public ArenaSelectorCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("select")) {
            Player player = (Player) sender;
            ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
            ItemMeta meta = blazeRod.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Arena Selector");
            blazeRod.setItemMeta(meta);
            player.getInventory().addItem(blazeRod);
            sender.sendMessage(ChatColor.BLUE + "You have been given an Arena Selector!");
            return true;
        } else if (command.getName().equalsIgnoreCase("create")) {
            Player player = (Player) sender;
            if (player.hasMetadata("arena_pos1") && player.hasMetadata("arena_pos2")) {
                Location pos1 = (Location) player.getMetadata("arena_pos1").get(0).value();
                Location pos2 = (Location) player.getMetadata("arena_pos2").get(0).value();
                // Create a region from pos1 and pos2
                // ...
                assert pos1 != null;
                plugin.getConfig().set("region.world", Objects.requireNonNull(pos1.getWorld()).getName());
                plugin.getConfig().set("region.pos1.x", pos1.getX());
                plugin.getConfig().set("region.pos1.y", pos1.getY());
                plugin.getConfig().set("region.pos1.z", pos1.getZ());
                assert pos2 != null;
                plugin.getConfig().set("region.pos2.x", pos2.getX());
                plugin.getConfig().set("region.pos2.y", pos2.getY());
                plugin.getConfig().set("region.pos2.z", pos2.getZ());
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Region created and saved to config.yml!");
            } else {
                sender.sendMessage(ChatColor.RED + "You must set both positions first using the Arena Selector!");
            }
            return true;
        }
        return false;
    }
}
