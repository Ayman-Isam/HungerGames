package me.aymanisam.hungergames.handler;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.commands.ChestRefillCommand;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveDisableHandler implements Listener {
    private final HungerGames plugin;
    private final ChestRefillCommand chestRefillCommand;
    private final PlayerSignClickManager playerSignClickManager;

    public MoveDisableHandler(HungerGames plugin, ChestRefillCommand chestRefillCommand, PlayerSignClickManager playerSignClickManager){
        this.plugin = plugin;
        this.chestRefillCommand = chestRefillCommand;
        this.playerSignClickManager = playerSignClickManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (playerSignClickManager.hasPlayerClickedSign(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            assert to != null;
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                FileConfiguration arenaConfig = chestRefillCommand.getArenaConfig();
                if (arenaConfig.contains("region.pos1.x") && arenaConfig.contains("region.pos1.y") && arenaConfig.contains("region.pos1.z") &&
                        arenaConfig.contains("region.pos2.x") && arenaConfig.contains("region.pos2.y") && arenaConfig.contains("region.pos2.z")) {
                    double x1 = arenaConfig.getDouble("region.pos1.x");
                    double y1 = arenaConfig.getDouble("region.pos1.y");
                    double z1 = arenaConfig.getDouble("region.pos1.z");
                    double x2 = arenaConfig.getDouble("region.pos2.x");
                    double y2 = arenaConfig.getDouble("region.pos2.y");
                    double z2 = arenaConfig.getDouble("region.pos2.z");

                    if (!plugin.gameStarted && to.getX() >= Math.min(x1, x2) && to.getX() <= Math.max(x1, x2) && to.getY() >= Math.min(y1, y2) && to.getY() <= Math.max(y1, y2) && to.getZ() >= Math.min(z1, z2) && to.getZ() <= Math.max(z1, z2)) {
                        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}