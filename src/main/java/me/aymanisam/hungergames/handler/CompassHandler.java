package me.aymanisam.hungergames.handler;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CompassHandler {

    public CompassHandler(HungerGames plugin) {

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.getType() == Material.COMPASS) {
                    Player nearestPlayer = findNearestPlayer(player);
                    if (nearestPlayer != null) {
                        player.setCompassTarget(nearestPlayer.getLocation());
                    }
                }
            }
        }, 0L, 20L);
    }

    private Player findNearestPlayer(Player player) {
        double closestDistance = Double.MAX_VALUE;
        Player closestPlayer = null;
        for (Player otherPlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (otherPlayer != player && otherPlayer.getWorld() == player.getWorld()) {
                double distance = player.getLocation().distance(otherPlayer.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = otherPlayer;
                }
            }
        }
        return closestPlayer;
    }
}
