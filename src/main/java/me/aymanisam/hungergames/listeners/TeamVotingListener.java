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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TeamVotingListener implements Listener {
    private final LangHandler langHandler;

    private Inventory inventory;

    public final Map<Player, String> playerVotes = new HashMap<>();

    public TeamVotingListener(HungerGames plugin) {
        this.langHandler = new LangHandler(plugin);
    }

    public void openVotingInventory(Player player) {
        ItemStack itemStack = new ItemStack(Material.BOOK);
        ItemMeta itemMeta = itemStack.getItemMeta();
        langHandler.getLangConfig(player);
        itemMeta.setDisplayName(langHandler.getMessage("game.voting-inv"));
        itemStack.setItemMeta(itemMeta);
        player.getInventory().setItem(8, itemStack);

        inventory = Bukkit.createInventory(null, 9, langHandler.getMessage("game.voting-inv"));

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

    public void closeVotingInventory(Player player) {
        player.getInventory().clear();
        player.closeInventory();
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
            playerVotes.put(player, "solo");
            player.sendMessage(langHandler.getMessage("game.voted-solo"));
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage("game.duo-inv"))) {
            playerVotes.put(player, "duo");
            player.sendMessage(langHandler.getMessage("game.voted-duo"));
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage("game.trio-inv"))) {
            playerVotes.put(player, "trio");
            player.sendMessage(langHandler.getMessage("game.voted-trio"));
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage("game.close-inv"))){
            player.closeInventory();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() != Material.BOOK) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        if (meta.getDisplayName().equals(langHandler.getMessage("game.voting-inv"))) {
            openVotingInventory(event.getPlayer());
        }
    }
}
