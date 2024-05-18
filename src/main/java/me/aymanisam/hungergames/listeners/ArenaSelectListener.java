package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

public class ArenaSelectListener implements Listener {
    private final HungerGames plugin;
    private final LangHandler langHandlerInstance;

    public ArenaSelectListener(LangHandler langHandlerInstance, HungerGames plugin) {
        this.langHandlerInstance = langHandlerInstance;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            langHandlerInstance.loadLanguageConfig(player);
            player.sendMessage(langHandlerInstance.getMessage("setarena.first-pos-1")
                    + Objects.requireNonNull(event.getClickedBlock()).getX() + langHandlerInstance.getMessage("setarena.first-pos-2")
                    + event.getClickedBlock().getY() + langHandlerInstance.getMessage("setarena.first-pos-3") + event.getClickedBlock().getZ());
            player.setMetadata("arena_pos1", new FixedMetadataValue(plugin, event.getClickedBlock().getLocation()));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            langHandlerInstance.loadLanguageConfig(player);
            player.sendMessage(langHandlerInstance.getMessage("setarena.second-pos-1")
                    + Objects.requireNonNull(event.getClickedBlock()).getX() + langHandlerInstance.getMessage("setarena.second-pos-2")
                    + event.getClickedBlock().getY() + langHandlerInstance.getMessage("setarena.second-pos-3") + event.getClickedBlock().getZ());
            player.setMetadata("arena_pos2", new FixedMetadataValue(plugin, event.getClickedBlock().getLocation()));
        }
    }
}
