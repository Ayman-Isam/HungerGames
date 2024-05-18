package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

public class ArenaSelectListener implements Listener {
    private final HungerGames plugin;
    private final LangHandler langHandler;

    public ArenaSelectListener(HungerGames plugin) {
        this.langHandler = new LangHandler(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        langHandler.loadLanguageConfig(player);
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.BLAZE_ROD && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(langHandler.getMessage("arena.stick-name"))) {
            if (!(player.hasPermission("hungergames.select"))) {
                player.sendMessage(langHandler.getMessage("no-permission"));
                return;
            }
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                player.sendMessage(langHandler.getMessage("setarena.first-pos-1") + Objects.requireNonNull(event.getClickedBlock()).getX() + langHandler.getMessage("setarena.first-pos-2") + event.getClickedBlock().getY() + langHandler.getMessage("setarena.first-pos-3") + event.getClickedBlock().getZ());
                player.setMetadata("arena_pos1", new FixedMetadataValue(plugin, event.getClickedBlock().getLocation()));
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                player.sendMessage(langHandler.getMessage("setarena.second-pos-1") + Objects.requireNonNull(event.getClickedBlock()).getX() + langHandler.getMessage("setarena.second-pos-2") + event.getClickedBlock().getY() + langHandler.getMessage("setarena.second-pos-3") + event.getClickedBlock().getZ());
                player.setMetadata("arena_pos2", new FixedMetadataValue(plugin, event.getClickedBlock().getLocation()));
            }
            event.setCancelled(true);
        }
    }
}
