package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.handlers.TeamsHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

import static me.aymanisam.hungergames.HungerGames.isGameStartingOrStarted;

public class TeamChatListener implements Listener {
    private final TeamsHandler teamsHandler;

    public TeamChatListener(TeamsHandler teamsHandler) {
        this.teamsHandler = teamsHandler;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();

        if ((isGameStartingOrStarted(sender.getWorld().getName())) && teamsHandler.isPlayerInAnyTeam(sender, sender.getWorld()) && teamsHandler.isChatModeEnabled(sender)) {
            List<Player> teammates = teamsHandler.getTeammates(sender, sender.getWorld());

            teammates.add(sender);

            event.setCancelled(true);

            String message = event.getMessage();
            String format = event.getFormat();

            for (Player teammate : teammates) {
                teammate.sendMessage(String.format(format, sender.getDisplayName(), message));
            }
        }
    }
}
