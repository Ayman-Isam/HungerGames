package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.CompassHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class CompassListener implements Listener {
    private final LangHandler langHandler;
    private final CompassHandler compassHandler;

    public CompassListener(HungerGames plugin, CompassHandler compassHandler) {
        this.compassHandler = compassHandler;
        this.langHandler = new LangHandler(plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                updateCompassTargets();
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }

    private void updateCompassTargets() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.COMPASS) {
                if (Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().equals(langHandler.getMessage("team.compass-teammate"))) {
                    Player nearestPlayer = compassHandler.findNearestTeammate(player, false);
                    if (nearestPlayer != null) {
                        player.setCompassTarget(nearestPlayer.getLocation());
                    }
                } else if (Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().equals(langHandler.getMessage("team.compass-enemy"))) {
                    Player nearestPlayer = compassHandler.findNearestEnemy(player, false);
                    if (nearestPlayer != null) {
                        player.setCompassTarget(nearestPlayer.getLocation());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.COMPASS && Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().equals(langHandler.getMessage("team.compass-teammate"))) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player nearestPlayer = compassHandler.findNearestTeammate(player, true);
                if (nearestPlayer != null) {
                    player.setCompassTarget(nearestPlayer.getLocation());
                }
            }
        } else if (itemInHand.getType() == Material.COMPASS && Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().equals(langHandler.getMessage("team.compass-enemy"))) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player nearestPlayer = compassHandler.findNearestEnemy(player, true);
                if (nearestPlayer != null) {
                    player.setCompassTarget(nearestPlayer.getLocation());
                }
            }
        }
    }
}
