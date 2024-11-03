package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArenaSelectCommand implements CommandExecutor {
    private final LangHandler langHandler;

    public ArenaSelectCommand(LangHandler langHandler) {
        this.langHandler = langHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(player.hasPermission("hungergames.select"))) {
            sender.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        ItemStack arenaSelector = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = arenaSelector.getItemMeta();
        assert meta != null;
        meta.setDisplayName(langHandler.getMessage(player, "arena.stick-name"));

        List<String> lore = new ArrayList<>();
        lore.add(langHandler.getMessage(player, "arena.stick-left"));
        lore.add(langHandler.getMessage(player, "arena.stick-right"));
        meta.setLore(lore);
        arenaSelector.setItemMeta(meta);

        player.getInventory().addItem(arenaSelector);
        sender.sendMessage(langHandler.getMessage(player, "arena.given-stick"));

        return true;
    }
}
