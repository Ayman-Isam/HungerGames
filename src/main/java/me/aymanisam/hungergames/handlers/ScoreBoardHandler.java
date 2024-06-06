package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.timeLeft;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;

public class ScoreBoardHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ArenaHandler arenaHandler;

    public ScoreBoardHandler(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.arenaHandler = new ArenaHandler(plugin);
    }

    private Score createScore(Objective objective, String messageKey, int timeLeft, int interval) {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        ChatColor color;
        if (timeLeft <= interval / 3) {
            color = ChatColor.RED;
        } else if (timeLeft <= 2 * interval / 3) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.GREEN;
        }

        return objective.getScore(langHandler.getMessage(messageKey) + color + timeFormatted);
    }

    public void getScoreBoard() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            langHandler.getLangConfig(player);
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            assert manager != null;
            Scoreboard scoreboard = manager.getNewScoreboard();

            int gameTimeConfig = plugin.getConfig().getInt("game-time");
            int borderShrinkTimeConfig = plugin.getConfig().getInt("border.start-time");
            int pvpTimeConfig = plugin.getConfig().getInt("grace-period");
            int chestRefillInterval = plugin.getConfig().getInt("chestrefill.interval");
            int supplyDropInterval = plugin.getConfig().getInt("supplydrop.interval");

            Objective objective;

            if (plugin.getConfig().getInt("players-per-team") > 1) {
                objective = scoreboard.registerNewObjective(langHandler.getMessage("game.score-name-team"), "dummy", langHandler.getMessage("game.score-name-team"), RenderType.INTEGER);
            } else {
                objective = scoreboard.registerNewObjective(langHandler.getMessage("game.score-name-solo"), "dummy", langHandler.getMessage("game.score-name-solo"), RenderType.INTEGER);
            }

            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            objective.getScore("  ").setScore(15);

            Score playersAliveScore = objective.getScore(langHandler.getMessage("game.score-alive") + ChatColor.GREEN + playersAlive.size());
            playersAliveScore.setScore(14);

            String worldName = arenaHandler.getArenaConfig().getString("region.world");
            if (worldName == null) {
                plugin.getLogger().log(Level.SEVERE, "World name is not specified.");
                return;
            }

            World world = plugin.getServer().getWorld(worldName);
            Score worldBorderSizeScore = objective.getScore(langHandler.getMessage("game.score-border") + ChatColor.GREEN + (int) world.getWorldBorder().getSize());
            worldBorderSizeScore.setScore(13);

            objective.getScore("  ").setScore(12);

            Score timeScore = createScore(objective, "game.score-time", timeLeft, gameTimeConfig);
            timeScore.setScore(11);

            int borderShrinkTimeLeft = (timeLeft - gameTimeConfig) + borderShrinkTimeConfig;
            Score borderShrinkScore = createScore(objective, "game.score-borderShrink", borderShrinkTimeLeft, borderShrinkTimeConfig);
            if (borderShrinkTimeLeft >= 0) {
                borderShrinkScore.setScore(10);
            }

            int pvpTimeLeft = (timeLeft - gameTimeConfig) + pvpTimeConfig;
            Score pvpScore = createScore(objective, "game.score-pvp", pvpTimeLeft, pvpTimeConfig);
            if (pvpTimeLeft >= 0) {
                pvpScore.setScore(9);
            }

            objective.getScore("").setScore(8);

            int chestRefillTimeLeft = timeLeft % chestRefillInterval;
            Score chestRefillScore = createScore(objective, "game.score-chestrefill", chestRefillTimeLeft, chestRefillInterval);
            if (chestRefillTimeLeft >= 0) {
                chestRefillScore.setScore(7);
            }

            int supplyDropTimeLeft = timeLeft % supplyDropInterval;
            Score supplyDropScore = createScore(objective, "game.score-supplydrop", supplyDropTimeLeft, supplyDropInterval);
            if (supplyDropTimeLeft >= 0) {
                supplyDropScore.setScore(6);
            }


            if (plugin.getConfig().getInt("players-per-team") > 1) {
                objective.getScore("").setScore(5);
                for (List<Player> team : teams) {
                    if (team.contains(player)) {
                        for (Player teamMember : team) {
                            if (!teamMember.equals(player)) {
                                String teammateName = teamMember.getName();
                                ChatColor color = playersAlive.contains(teamMember) ? ChatColor.GREEN : ChatColor.RED;
                                String scoreName = langHandler.getMessage("game.score-teammate") + color + teammateName;
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
