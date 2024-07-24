package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TeamVotingListener implements Listener {
    private final LangHandler langHandler;

    public static final Map<Player, String> playerVotes = new HashMap<>();

    public TeamVotingListener(HungerGames plugin) {
        this.langHandler = new LangHandler(plugin);
    }

    public void openVotingInventory(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        giveVotingBook(player, langHandler);

        Inventory inventory = Bukkit.createInventory(null, 9, langHandler.getMessage("team.voting-inv"));

        ItemStack solo = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta soloMeta = solo.getItemMeta();
        assert soloMeta != null;
        soloMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage("team.solo-inv"));
        soloMeta.setLore(Collections.singletonList(langHandler.getMessage("team.votes", getVoteCount("solo"))));
        soloMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        solo.setItemMeta(soloMeta);

        ItemStack duo = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta duoMeta = duo.getItemMeta();
        assert duoMeta != null;
        duoMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage("team.duo-inv"));
        duoMeta.setLore(Collections.singletonList(langHandler.getMessage("team.votes", getVoteCount("duo"))));
        duoMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        duo.setItemMeta(duoMeta);

        ItemStack trio = new ItemStack(Material.IRON_SWORD);
        ItemMeta trioMeta = trio.getItemMeta();
        assert trioMeta != null;
        trioMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage("team.trio-inv"));
        trioMeta.setLore(Collections.singletonList(langHandler.getMessage("team.votes", getVoteCount("trio"))));
        trioMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        trio.setItemMeta(trioMeta);

        ItemStack versus = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta versusMeta = versus.getItemMeta();
        assert versusMeta != null;
        versusMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage("team.versus-inv"));
        versusMeta.setLore(Collections.singletonList(langHandler.getMessage("team.votes", getVoteCount("versus"))));
        versusMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        versus.setItemMeta(versusMeta);

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        assert backMeta != null;
        backMeta.setDisplayName(ChatColor.RED + langHandler.getMessage("team.close-inv"));
        backButton.setItemMeta(backMeta);

        inventory.setItem(2, solo);
        inventory.setItem(3, duo);
        inventory.setItem(4, trio);
        inventory.setItem(5, versus);
        inventory.setItem(8, backButton);

        player.openInventory(inventory);
    }

    public void closeVotingInventory(Player player) {
        player.getInventory().clear();
        player.closeInventory();
    }

    private long getVoteCount(String vote) {
        return playerVotes.values().stream().filter(vote::equals).count();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(langHandler.getMessage("team.voting-inv")) || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        event.setCancelled(true);

        String displayName = Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName();

        if (displayName.equals(langHandler.getMessage("team.solo-inv"))) {
            playerVotes.put(player, "solo");
            player.sendMessage(langHandler.getMessage("team.voted-solo"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage("team.duo-inv"))) {
            playerVotes.put(player, "duo");
            player.sendMessage(langHandler.getMessage("team.voted-duo"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage("team.trio-inv"))) {
            playerVotes.put(player, "trio");
            player.sendMessage(langHandler.getMessage("team.voted-trio"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage("team.versus-inv"))) {
            playerVotes.put(player, "versus");
            player.sendMessage(langHandler.getMessage("team.voted-versus"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage("team.close-inv"))){
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            player.closeInventory();
        }
    }

    public static void giveVotingBook(Player player, LangHandler langHandler) {
        ItemStack itemStack = new ItemStack(Material.BOOK);
        ItemMeta itemMeta = itemStack.getItemMeta();
        langHandler.getLangConfig(player);
        assert itemMeta != null;
        itemMeta.setDisplayName(langHandler.getMessage("team.voting-inv"));
        itemStack.setItemMeta(itemMeta);
        player.getInventory().setItem(8, itemStack);
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

        if (meta.getDisplayName().equals(langHandler.getMessage("team.voting-inv"))) {
            openVotingInventory(event.getPlayer());
        }
    }
}
