package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CompassHandler {
    public CompassHandler (HungerGames plugin) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.getType() == Material.COMPASS) {
                    // If player has compass in main hand, get nearest player
                    Player nearestPlayer = findNearestPlayer(player);
                    if (nearestPlayer != null) {
                        player.setCompassTarget(nearestPlayer.getLocation());
                    }
                }
            }
        }, 0L, 5L);
    }

    private Player findNearestPlayer (Player player){
        double closestDistance = Double.MAX_VALUE;
        Player closestPlayer = null;
        for (Player targetPlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (targetPlayer != player && targetPlayer.getGameMode() == GameMode.ADVENTURE && targetPlayer.isOnline()) {
                // Only tracking other players, in adventure and not offline
                double distance = player.getLocation().distance(targetPlayer.getLocation());
                // If players are in different worlds, skip
                if (player.getWorld() != targetPlayer.getWorld()) continue;
                // If distance from one player is smaller than other player, set it as closest
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = targetPlayer;
                }
            }
        }
        return closestPlayer;
    }
}
