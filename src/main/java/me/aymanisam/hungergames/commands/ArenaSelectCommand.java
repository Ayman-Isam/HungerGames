package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArenaSelectCommand implements CommandExecutor {
    private final LangHandler langHandler;

    public ArenaSelectCommand(HungerGames plugin) {
        this.langHandler = new LangHandler(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
            return true;
        }

        langHandler.loadLanguageConfig(player);

        if (!(player.hasPermission("hungergames.select"))) {
            sender.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = blazeRod.getItemMeta();
        assert meta != null;
        meta.setDisplayName(langHandler.getMessage("arena.stick-name"));
        List<String> lore = new ArrayList<>();
        lore.add(langHandler.getMessage("arena.stick-left"));
        lore.add(langHandler.getMessage("arena.stick-right"));
        meta.setLore(lore);
        blazeRod.setItemMeta(meta);
        player.getInventory().addItem(blazeRod);
        sender.sendMessage(langHandler.getMessage("arena.given-stick"));

        return true;
    }
}
