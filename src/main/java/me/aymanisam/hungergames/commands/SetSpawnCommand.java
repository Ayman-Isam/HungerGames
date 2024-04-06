package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handler.SetSpawnHandler;
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
        if (sender instanceof Player player) {
            plugin.loadLanguageConfig(player);
            if (player.hasPermission("hungergames.setspawn")) {
                ItemStack stick = new ItemStack(Material.STICK);
                ItemMeta meta = stick.getItemMeta();
                assert meta != null;
                meta.setDisplayName(plugin.getMessage("setspawn.stick-name"));
                stick.setItemMeta(meta);
                player.getInventory().addItem(stick);
                sender.sendMessage(plugin.getMessage("setspawn.given-stick"));
                SetSpawnHandler setSpawnHandler = plugin.getSetSpawnHandler();
                setSpawnHandler.getSetSpawnConfig().set("spawnpoints", new ArrayList<>());
                setSpawnHandler.saveSetSpawnConfig();
                setSpawnHandler.resetSpawnPoints();
                sender.sendMessage(plugin.getMessage("setspawn.spawn-reset"));
                return true;
            } else {
                sender.sendMessage(plugin.getMessage("no-permission"));
            }
        }
        return true;
    }
}

