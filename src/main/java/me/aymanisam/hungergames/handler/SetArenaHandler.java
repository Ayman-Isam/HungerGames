package me.aymanisam.hungergames.handler;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

public class SetArenaHandler implements Listener {
    private final HungerGames plugin;

    public SetArenaHandler(HungerGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.BLAZE_ROD && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(plugin.getMessage("arena.stick-name"))) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                plugin.loadLanguageConfig(player);
                player.sendMessage(plugin.getMessage("setarena.first-pos-1") + Objects.requireNonNull(event.getClickedBlock()).getX() + plugin.getMessage("setarena.first-pos-2") + event.getClickedBlock().getY() + plugin.getMessage("setarena.first-pos-3") + event.getClickedBlock().getZ());
                player.setMetadata("arena_pos1", new FixedMetadataValue(plugin, event.getClickedBlock().getLocation()));
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                plugin.loadLanguageConfig(player);
                player.sendMessage(plugin.getMessage("setarena.second-pos-1") + Objects.requireNonNull(event.getClickedBlock()).getX() + plugin.getMessage("setarena.second-pos-2") + event.getClickedBlock().getY() + plugin.getMessage("setarena.second-pos-3") + event.getClickedBlock().getZ());
                player.setMetadata("arena_pos2", new FixedMetadataValue(plugin, event.getClickedBlock().getLocation()));
            }
            event.setCancelled(true);
        }
    }
}

