package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class TeamsHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;

    public static final List<List<Player>> teams = new ArrayList<>();
    public static final List<List<Player>> teamsAlive = new ArrayList<>();

    public TeamsHandler(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
    }

    public void createTeam() {
        if (plugin.getConfig().getInt("players-per-team") > 1) {
            int teamSize = plugin.getConfig().getInt("players-per-team");
            int numTeams = (playersAlive.size() + teamSize - 1) / teamSize;
            Collections.shuffle(playersAlive);
            teams.clear();

            for (int i = 0; i < numTeams; i++) {
                teams.add(new ArrayList<>());
            }

            for (int i = 0; i < playersAlive.size(); i++) {
                Player player = playersAlive.get(i);
                List<Player> team = teams.get(i % numTeams);
                team.add(player);
            }

            for (List<Player> team : teams) {
                List<Player> teamCopy = new ArrayList<>(team);
                teamsAlive.add(teamCopy);
            }

            for (int i = 0; i < numTeams; i++) {
                List<Player> team = teams.get(i);
                for (Player player : team) {
                    langHandler.getLangConfig(player);
                    player.sendMessage(langHandler.getMessage("game.team-id") + (i + 1));
                    player.sendMessage(langHandler.getMessage("game.team-members") + getTeammateNames(team, player));
                }
            }
        }
    }

    private String getTeammateNames(List<Player> team, Player currentPlayer) {
        StringBuilder teammates = new StringBuilder();
        for (Player player : team) {
            if (player != currentPlayer) {
                if (!teammates.isEmpty()) {
                    teammates.append(", ");
                }
                teammates.append(player.getName());
            }
        }
        return teammates.toString();
    }
}
