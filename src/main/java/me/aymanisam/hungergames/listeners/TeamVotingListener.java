package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TeamVotingListener implements Listener {
    private final LangHandler langHandler;

    public static final Map<World,Map<Player, String>> playerVotes = new HashMap<>();

    public TeamVotingListener(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
    }

    public void openVotingInventory(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        giveVotingBook(player, langHandler);

        Inventory inventory = Bukkit.createInventory(null, 9, langHandler.getMessage(player, "team.voting-inv"));

        ItemStack solo = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta soloMeta = solo.getItemMeta();
        assert soloMeta != null;
        soloMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage(player, "team.solo-inv"));
        soloMeta.setLore(Collections.singletonList(langHandler.getMessage(player, "team.votes", getVoteCount("solo", player.getWorld()))));
        soloMeta.setAttributeModifiers(Material.NETHERITE_SWORD.getDefaultAttributeModifiers(EquipmentSlot.CHEST));
        soloMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        solo.setItemMeta(soloMeta);

        ItemStack duo = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta duoMeta = duo.getItemMeta();
        assert duoMeta != null;
        duoMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage(player, "team.duo-inv"));
        duoMeta.setLore(Collections.singletonList(langHandler.getMessage(player, "team.votes", getVoteCount("duo", player.getWorld()))));
        duoMeta.setAttributeModifiers(Material.DIAMOND_SWORD.getDefaultAttributeModifiers(EquipmentSlot.CHEST));
        duoMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        duo.setItemMeta(duoMeta);

        ItemStack trio = new ItemStack(Material.IRON_SWORD);
        ItemMeta trioMeta = trio.getItemMeta();
        assert trioMeta != null;
        trioMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage(player, "team.trio-inv"));
        trioMeta.setLore(Collections.singletonList(langHandler.getMessage(player, "team.votes", getVoteCount("trio", player.getWorld()))));
        trioMeta.setAttributeModifiers(Material.IRON_SWORD.getDefaultAttributeModifiers(EquipmentSlot.CHEST));
        trioMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        trio.setItemMeta(trioMeta);

        ItemStack versus = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta versusMeta = versus.getItemMeta();
        assert versusMeta != null;
        versusMeta.setDisplayName(ChatColor.GREEN + langHandler.getMessage(player, "team.versus-inv"));
        versusMeta.setLore(Collections.singletonList(langHandler.getMessage(player, "team.votes", getVoteCount("versus", player.getWorld()))));
        versusMeta.setAttributeModifiers(Material.GOLDEN_SWORD.getDefaultAttributeModifiers(EquipmentSlot.CHEST));
        versusMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        versus.setItemMeta(versusMeta);

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        assert backMeta != null;
        backMeta.setDisplayName(ChatColor.RED + langHandler.getMessage(player, "team.close-inv"));
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

    private long getVoteCount(String vote, World world) {
        Map<Player, String> worldPlayerVotes = playerVotes.computeIfAbsent(world, k -> new HashMap<>());
        return worldPlayerVotes.values().stream().filter(vote::equals).count();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equals(langHandler.getMessage(player, "team.voting-inv"))) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        event.setCancelled(true);

        if (event.getClick().isKeyboardClick()) {
            return;
        }

        String displayName = Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName();

        World world = player.getWorld();

        if (displayName.equals(langHandler.getMessage(player, "team.solo-inv"))) {
            Map<Player, String> votes = playerVotes.computeIfAbsent(world, k -> new HashMap<>());
            votes.put(player, "solo");
            player.sendMessage(langHandler.getMessage(player, "team.voted-solo"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage(player, "team.duo-inv"))) {
            Map<Player, String> votes = playerVotes.computeIfAbsent(world, k -> new HashMap<>());
            votes.put(player, "duo");
            player.sendMessage(langHandler.getMessage(player, "team.voted-duo"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage(player, "team.trio-inv"))) {
            Map<Player, String> votes = playerVotes.computeIfAbsent(world, k -> new HashMap<>());
            votes.put(player, "trio");
            player.sendMessage(langHandler.getMessage(player, "team.voted-trio"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage(player, "team.versus-inv"))) {
            Map<Player, String> votes = playerVotes.computeIfAbsent(world, k -> new HashMap<>());
            votes.put(player, "versus");
            player.sendMessage(langHandler.getMessage(player, "team.voted-versus"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.closeInventory();
        } else if (displayName.equals(langHandler.getMessage(player, "team.close-inv"))) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            player.closeInventory();
        }
    }

    public static void giveVotingBook(Player player, LangHandler langHandler) {
        ItemStack itemStack = new ItemStack(Material.BOOK);
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(langHandler.getMessage(player, "team.voting-inv"));
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

        Player player = event.getPlayer();

        if (meta.getDisplayName().equals(langHandler.getMessage(player, "team.voting-inv"))) {
            openVotingInventory(event.getPlayer());
        }
    }
}
