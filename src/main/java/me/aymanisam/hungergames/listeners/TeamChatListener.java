package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.handlers.TeamsHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

import static me.aymanisam.hungergames.HungerGames.gameStarted;

public class TeamChatListener implements Listener {
    private final TeamsHandler teamsHandler;

    public TeamChatListener(TeamsHandler teamsHandler) {
        this.teamsHandler = teamsHandler;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();

        if (gameStarted && teamsHandler.isPlayerInAnyTeam(sender) && teamsHandler.isChatModeEnabled(sender)) {
            event.setCancelled(true);

            List<Player> teammates = teamsHandler.getTeammates(sender);

            String message = event.getMessage();

            for (Player teammate : teammates) {
                teammate.sendMessage(message);
            }
        }
    }
}
