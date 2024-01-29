package me.cantankerousally.hungergames.commands;

import me.cantankerousally.hungergames.HungerGames;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class MoveToggleCommand implements CommandExecutor, Listener {
    private final HungerGames plugin;
    private final ChestRefillCommand chestRefillCommand;

    private boolean moveToggle = true;

    public MoveToggleCommand(HungerGames plugin, ChestRefillCommand chestRefillCommand){
        this.plugin = plugin;
        this.chestRefillCommand = chestRefillCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        moveToggle = !moveToggle;
        if (moveToggle) {
            commandSender.sendMessage(ChatColor.GREEN + "MoveToggle is now enabled.");
        } else {
            commandSender.sendMessage(ChatColor.RED + "MoveToggle is now disabled.");
        }
        return true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!moveToggle) {
            return;
        }
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        assert to != null;
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
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

