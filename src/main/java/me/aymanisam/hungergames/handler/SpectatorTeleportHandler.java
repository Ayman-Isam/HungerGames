package me.aymanisam.hungergames.handler;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpectatorTeleportHandler implements Listener {
    private final Map<Integer, Player> slotPlayerMap = new HashMap<>();
    private final HungerGames plugin;
    private final GameHandler gameHandler;

    public SpectatorTeleportHandler(HungerGames plugin, GameHandler gameHandler) {
        this.plugin = plugin;
        this.gameHandler = gameHandler;
    }

    public void openSpectatorGUI(Player spectator) {
        slotPlayerMap.clear();

        List<Player> playersAlive = gameHandler.getPlayersAlive();
        if (playersAlive.isEmpty()) {
            spectator.sendMessage(plugin.getMessage("spectate.no-player"));
            return;
        }
        int size = (int) Math.ceil(playersAlive.size() / 9.0) * 9;
        Inventory gui = Bukkit.createInventory(null, size, plugin.getMessage("spectate.gui-message"));
        for (int i = 0; i < playersAlive.size(); i++) {
            Player player = playersAlive.get(i);
            ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerItem.getItemMeta();
            meta.setOwningPlayer(player);
            meta.setDisplayName(player.getName());
            playerItem.setItemMeta(meta);

            gui.setItem(i, playerItem);
            slotPlayerMap.put(i, player);
        }
        spectator.openInventory(gui);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory openInventory = player.getOpenInventory().getTopInventory();

        if (clickedInventory != null && clickedInventory.equals(openInventory)) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.PLAYER_HEAD) {
                event.setCancelled(true);

                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String playerName = meta.getDisplayName();
                    Player target = Bukkit.getPlayerExact(playerName);
                    if (target != null) {
                        player.teleport(target.getLocation());
                        player.sendMessage("Teleported to " + playerName);
                    } else {
                        player.sendMessage("Player not found or offline.");
                    }
                }
            }
        }
    }
}
