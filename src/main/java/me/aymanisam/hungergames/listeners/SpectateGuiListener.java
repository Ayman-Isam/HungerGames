package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpectateGuiListener implements Listener {
    private final HungerGames plugin;
    private final LangHandler langHandler;

    public SpectateGuiListener(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
    }

    @EventHandler
    public void onGuiOpen(InventoryClickEvent event){
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

                    if (target == null) {
                        player.sendMessage(langHandler.getMessage("spectate.null-player"));
                        return;
                    }

                    player.teleport(target.getLocation());
                    player.sendMessage(langHandler.getMessage("spectate.teleported") + playerName);
                }
            }
        }
    }
}
