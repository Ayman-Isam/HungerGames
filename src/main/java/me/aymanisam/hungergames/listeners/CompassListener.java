package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.CompassHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CompassListener implements Listener {
    private final LangHandler langHandler;
    private final CompassHandler compassHandler;

    public CompassListener(HungerGames plugin, CompassHandler compassHandler) {
        this.compassHandler = compassHandler;
        this.langHandler = new LangHandler(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.COMPASS && Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().equals(langHandler.getMessage("game.compass-teammate"))) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player nearestPlayer = compassHandler.findNearestTeammate(player);
                if (nearestPlayer != null) {
                    player.setCompassTarget(nearestPlayer.getLocation());
                }
            }
        } else if (itemInHand.getType() == Material.COMPASS && Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().equals(langHandler.getMessage("game.compass-enemy"))) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player nearestPlayer = compassHandler.findNearestEnemy(player);
                if (nearestPlayer != null) {
                    player.setCompassTarget(nearestPlayer.getLocation());
                }
            }
        }
    }
}
