package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.handlers.TeamsHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

import static me.aymanisam.hungergames.HungerGames.gameStarted;
import static me.aymanisam.hungergames.HungerGames.gameStarting;

public class TeamChatListener implements Listener {
    private final TeamsHandler teamsHandler;

    public TeamChatListener(TeamsHandler teamsHandler) {
        this.teamsHandler = teamsHandler;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();

        if ((gameStarted || gameStarting) && teamsHandler.isPlayerInAnyTeam(sender) && teamsHandler.isChatModeEnabled(sender)) {
            List<Player> teammates = teamsHandler.getTeammates(sender);

            teammates.add(sender);

            event.setCancelled(true);

            String message = event.getMessage();
            String format = event.getFormat();

            for (Player teammate : teammates) {
                teammate.sendMessage(String.format(format, sender.getDisplayName(), message));
            }

            System.out.println("Modified Recipients: " + teammates);
        }
    }
}
