package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class SetSpawnCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;

    public SetSpawnCommand(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.langHandler = new LangHandler(plugin);
        this.setSpawnHandler = setSpawnHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("player-only"));
            return true;
        }

        langHandler.getLangConfig(player);

        if (!(player.hasPermission("hungergames.setspawn"))) {
            sender.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        assert meta != null;
        meta.setDisplayName(langHandler.getMessage("setspawn.stick-name"));
        stick.setItemMeta(meta);
        player.getInventory().addItem(stick);
        setSpawnHandler.setSpawnConfig.set("spawnpoints", new ArrayList<>());
        setSpawnHandler.saveSetSpawnConfig();
        setSpawnHandler.resetSpawnPoints();
        sender.sendMessage(langHandler.getMessage("setspawn.spawn-reset"));
        return true;
    }
}
