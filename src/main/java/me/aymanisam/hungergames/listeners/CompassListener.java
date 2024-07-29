package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.CompassHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.ScoreBoardHandler;
import me.aymanisam.hungergames.handlers.TeamsHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class CompassListener implements Listener {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final CompassHandler compassHandler;
    private final TeamsHandler teamsHandler;
    private final Map<Player, BukkitTask> glowTasks = new HashMap<>();

    public CompassListener(HungerGames plugin, CompassHandler compassHandler, ScoreBoardHandler scoreBoardHandler) {
        this.plugin = plugin;
        this.compassHandler = compassHandler;
        this.langHandler = new LangHandler(plugin);
        this.teamsHandler = new TeamsHandler(plugin, scoreBoardHandler);

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
                    trackPlayer(player, nearestPlayer, false);
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
                trackPlayer(player, nearestPlayer, true);
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

    private void trackPlayer(Player player, Player nearestPlayer, Boolean glow) {
        if (nearestPlayer != null) {
            player.setCompassTarget(nearestPlayer.getLocation());
            if (glow) {
                teamsHandler.playerGlow(nearestPlayer, player, true);

                // Cancel previous task if it exists
                if (glowTasks.containsKey(player)) {
                    glowTasks.get(player).cancel();
                }

                // Schedule new task and store it in the map
                BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    teamsHandler.playerGlow(nearestPlayer, player, false);
                }, 200L);
                glowTasks.put(player, task);
            }
        }
    }

    public void cancelGlowTask() {
        for (BukkitTask task : glowTasks.values()) {
            task.cancel();
        }
        glowTasks.clear();
    }
}
