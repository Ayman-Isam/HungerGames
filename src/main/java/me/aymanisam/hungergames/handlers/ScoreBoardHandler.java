package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.handlers.CountDownHandler.playersPerTeam;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.timeLeft;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;

public class ScoreBoardHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;

    public static Map<World, Integer> startingPlayers = new HashMap<>();

    public ScoreBoardHandler(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.configHandler = plugin.getConfigHandler();
    }

    private ChatColor getColor(int interval, int countdown) {
        ChatColor color;
        if (countdown <= interval / 3) {
            color = ChatColor.RED;
        } else if (countdown <= 2 * interval / 3) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.GREEN;
        }

        return color;
    }

    private Score createScore(Player player, Objective objective, String messageKey, int countdown, int interval) {
        int minutes = countdown / 60;
        int seconds = countdown % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        return objective.getScore(langHandler.getMessage(player, messageKey, getColor(interval, countdown) + timeFormatted));
    }

    public void getScoreBoard(World world) {
        FileConfiguration worldConfig = configHandler.getWorldConfig(world);
        int gameTimeConfig = worldConfig.getInt("game-time");
        int borderShrinkTimeConfig = worldConfig.getInt("border.start-time");
        int pvpTimeConfig = worldConfig.getInt("grace-period");
        int chestRefillInterval = worldConfig.getInt("chestrefill.interval");
        int supplyDropInterval = worldConfig.getInt("supplydrop.interval");
        int borderStartSize = worldConfig.getInt("border.size");
        int borderEndSize = worldConfig.getInt("border.final-size");
        int worldTimeLeft = timeLeft.get(world);

        int worldPlayersAliveSize = playersAlive.computeIfAbsent(world, k -> new ArrayList<>()).size();
        int worldBorderSize = (int) world.getWorldBorder().getSize();
        int borderShrinkTimeLeft = (worldTimeLeft - gameTimeConfig) + borderShrinkTimeConfig;
        int pvpTimeLeft = (worldTimeLeft - gameTimeConfig) + pvpTimeConfig;
        int chestRefillTimeLeft = worldTimeLeft % chestRefillInterval;
        int supplyDropTimeLeft = worldTimeLeft % supplyDropInterval;

        ChatColor borderColor;

        for (Player player : world.getPlayers()) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            assert manager != null;
            Scoreboard scoreboard = manager.getNewScoreboard();

            Objective objective;

            if (playersPerTeam != 1) {
                objective = scoreboard.registerNewObjective(langHandler.getMessage(player, "score.name-team"), "dummy", langHandler.getMessage(player, "score.name-team"), RenderType.INTEGER);
            } else {
                objective = scoreboard.registerNewObjective(langHandler.getMessage(player, "score.name-solo"), "dummy", langHandler.getMessage(player, "score.name-solo"), RenderType.INTEGER);
            }

            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            objective.getScore("  ").setScore(15);

            int worldStartingPlayers = startingPlayers.get(world);

            Score playersAliveScore = objective.getScore(langHandler.getMessage(player, "score.alive", getColor(worldStartingPlayers, worldPlayersAliveSize).toString() + worldPlayersAliveSize));
            playersAliveScore.setScore(14);

            if (borderStartSize == worldBorderSize) {
                borderColor = ChatColor.GREEN;
            } else if (borderEndSize == worldBorderSize) {
                borderColor = ChatColor.RED;
            } else {
                borderColor = ChatColor.YELLOW;
            }

            Score worldBorderSizeScore = objective.getScore(langHandler.getMessage(player, "score.border", borderColor.toString() + worldBorderSize));
            worldBorderSizeScore.setScore(13);

            objective.getScore("  ").setScore(12);

            Score timeScore = createScore(player, objective, "score.time", worldTimeLeft, gameTimeConfig);
            timeScore.setScore(11);

            Score borderShrinkScore = createScore(player, objective, "score.borderShrink", borderShrinkTimeLeft, borderShrinkTimeConfig);
            if (borderShrinkTimeLeft >= 0) {
                borderShrinkScore.setScore(10);
            }

            Score pvpScore = createScore(player, objective, "score.pvp", pvpTimeLeft, pvpTimeConfig);
            if (pvpTimeLeft >= 0) {
                pvpScore.setScore(9);
            }

            objective.getScore("").setScore(8);

            Score chestRefillScore = createScore(player, objective, "score.chestrefill", chestRefillTimeLeft, chestRefillInterval);
            if (chestRefillTimeLeft >= 0) {
                chestRefillScore.setScore(7);
            }

            Score supplyDropScore = createScore(player, objective, "score.supplydrop", supplyDropTimeLeft, supplyDropInterval);
            if (supplyDropTimeLeft >= 0) {
                supplyDropScore.setScore(6);
            }

            List<List<Player>> worldTeams = teams.computeIfAbsent(world, k -> new ArrayList<>());
            List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());

            if (playersPerTeam > 1) {
                objective.getScore("").setScore(5);
                for (List<Player> team : worldTeams) {
                    if (team.contains(player)) {
                        for (Player teamMember : team) {
                            if (!teamMember.equals(player)) {
                                String teammateName = teamMember.getName();
                                ChatColor color = worldPlayersAlive.contains(teamMember) ? ChatColor.GREEN : ChatColor.RED;
                                String scoreName = langHandler.getMessage(player, "score.teammate", color + teammateName);
                                Score teammateScore = objective.getScore(scoreName);
                                teammateScore.setScore(4);
                            }
                        }
                        break;
                    }
                }
            }

            player.setScoreboard(scoreboard);
        }
    }

    public void removeScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard mainScoreboard = manager.getMainScoreboard();
        player.setScoreboard(mainScoreboard);
    }
}
