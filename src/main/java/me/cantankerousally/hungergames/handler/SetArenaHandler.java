package me.cantankerousally.hungergames.handler;

import me.cantankerousally.hungergames.HungerGames;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class SetArenaHandler implements Listener {
    private HungerGames plugin;

    public SetArenaHandler(HungerGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.BLAZE_ROD && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(ChatColor.AQUA + "Arena Selector")) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "First position set at X=" + event.getClickedBlock().getX() + ", Y=" + event.getClickedBlock().getY() + ", Z=" + event.getClickedBlock().getZ() + ".");
                player.setMetadata("arena_pos1", new FixedMetadataValue(plugin, event.getClickedBlock().getLocation()));
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Second position set at X=" + event.getClickedBlock().getX() + ", Y=" + event.getClickedBlock().getY() + ", Z=" + event.getClickedBlock().getZ() + ".");
                player.setMetadata("arena_pos2", new FixedMetadataValue(plugin, event.getClickedBlock().getLocation()));
            }
            event.setCancelled(true);
        }
    }
}

