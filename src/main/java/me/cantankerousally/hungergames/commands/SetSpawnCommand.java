package me.cantankerousally.hungergames.commands;

import me.cantankerousally.hungergames.HungerGames;
import me.cantankerousally.hungergames.handler.SetSpawnHandler;
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

public class SetSpawnCommand implements CommandExecutor {

    private final HungerGames plugin;

    public SetSpawnCommand(HungerGames plugin) {
        this.plugin = plugin;
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
            SetSpawnHandler setSpawnHandler = plugin.getSetSpawnHandler();
            setSpawnHandler.getSetSpawnConfig().set("spawnpoints", new ArrayList<>());
            setSpawnHandler.saveSetSpawnConfig();
            sender.sendMessage(ChatColor.GREEN + "Spawn points have been reset.");
            return true;
        }
        return false;
    }
}

