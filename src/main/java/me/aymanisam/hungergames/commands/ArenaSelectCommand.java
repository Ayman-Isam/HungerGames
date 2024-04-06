package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArenaSelectCommand implements CommandExecutor {
    private final HungerGames plugin;
    private FileConfiguration arenaConfig = null;
    private File arenaFile = null;


    public ArenaSelectCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            plugin.loadLanguageConfig(player);
            if (player.hasPermission("hungergames.select")) {
                ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
                ItemMeta meta = blazeRod.getItemMeta();
                assert meta != null;
                meta.setDisplayName(plugin.getMessage("arena.stick-name"));
                List<String> lore = new ArrayList<>();
                lore.add(plugin.getMessage("arena.stick-left"));
                lore.add(plugin.getMessage("arena.stick-right"));
                meta.setLore(lore);
                blazeRod.setItemMeta(meta);
                player.getInventory().addItem(blazeRod);
                sender.sendMessage(plugin.getMessage("arena.given-stick"));
            } else {
                sender.sendMessage(plugin.getMessage("no-permission"));
            }
        }
        else {
            sender.sendMessage(plugin.getMessage("no-server"));
        }
        return true;
    }
}