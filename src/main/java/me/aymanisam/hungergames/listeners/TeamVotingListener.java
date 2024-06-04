package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class TeamVotingListener implements Listener {
    private final LangHandler langHandler;

    private Inventory inventory;

    public int votedSolo = 0;
    public int votedDuo = 0;
    public int votedTrio = 0;

    public TeamVotingListener(HungerGames plugin) {
        this.langHandler = new LangHandler(plugin);
    }

    public void openVotingInventory(Player player) {
        player.getInventory().clear();

        inventory = Bukkit.createInventory(null, 9, langHandler.getMessage("game.voting-inv"));
        langHandler.getLangConfig(player);

        ItemStack solo = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta soloMeta = solo.getItemMeta();
        soloMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage("game.solo-inv"));
        soloMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        solo.setItemMeta(soloMeta);

        ItemStack duo = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta duoMeta = duo.getItemMeta();
        duoMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage("game.duo-inv"));
        duoMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        duo.setItemMeta(duoMeta);

        ItemStack trio = new ItemStack(Material.IRON_SWORD);
        ItemMeta trioMeta = trio.getItemMeta();
        trioMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage("game.trio-inv"));
        trioMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        trio.setItemMeta(trioMeta);

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + langHandler.getMessage("game.close-inv"));
        backButton.setItemMeta(backMeta);

        inventory.setItem(3, solo);
        inventory.setItem(4, duo);
        inventory.setItem(5, trio);
        inventory.setItem(8, backButton);

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(langHandler.getMessage("game.voting-inv")) || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        event.setCancelled(true);

        String displayName = Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName();

        if (displayName.equals(langHandler.getMessage("game.solo-inv"))) {
            player.sendMessage(langHandler.getMessage("game.voted-solo"));
            votedSolo += 1;
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage("game.duo-inv"))) {
            player.sendMessage(langHandler.getMessage("game.voted-duo"));
            votedDuo += 1;
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage("game.trio-inv"))) {
            player.sendMessage(langHandler.getMessage("game.voted-trio"));
            votedTrio += 1;
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage("game.close-inv"))){
            player.closeInventory();
        }
    }
}
