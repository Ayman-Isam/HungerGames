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

import java.util.ArrayList;
import java.util.Objects;

public class SetSpawnCommand implements CommandExecutor {

    public SetSpawnCommand() {
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setspawn")) {
            Player player = (Player) sender;
            ItemStack stick = new ItemStack(Material.STICK);
            ItemMeta meta = stick.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + "Spawn Point Selector");
            stick.setItemMeta(meta);
            player.getInventory().addItem(stick);
            sender.sendMessage(ChatColor.BLUE + "You have been given a Spawn Point Selector!");
            Objects.requireNonNull(player.getServer().getPluginManager().getPlugin("HungerGames")).getConfig().set("spawnpoints", new ArrayList<>());
            Objects.requireNonNull(player.getServer().getPluginManager().getPlugin("HungerGames")).saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Spawn points have been reset.");
            return true;
        }
        return false;
    }
}

