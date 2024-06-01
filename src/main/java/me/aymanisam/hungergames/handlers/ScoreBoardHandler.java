package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Objects;

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

    public void getScoreBoard() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            langHandler.getLangConfig(player);
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            assert manager != null;
            Scoreboard scoreboard = manager.getNewScoreboard();

            Objective objective = scoreboard.registerNewObjective(langHandler.getMessage("game.score-name"), "dummy", langHandler.getMessage("game.score-name"), RenderType.INTEGER);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            objective.getScore("  ").setScore(15);

            Score playersAliveScore = objective.getScore(langHandler.getMessage("game.score-alive") + ChatColor.GREEN + playersAlive.size());
            playersAliveScore.setScore(14);

            World world = plugin.getServer().getWorld(Objects.requireNonNull(arenaHandler.getArenaConfig().getString("region.world")));
            assert world != null;
            Score worldBorderSizeScore = objective.getScore(langHandler.getMessage("game.score-border") + ChatColor.GREEN + (int) world.getWorldBorder().getSize());
            worldBorderSizeScore.setScore(13);

            objective.getScore("  ").setScore(12);

            String timeFormatted = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
            Score timeScore = objective.getScore(langHandler.getMessage("game.score-time") + ChatColor.GREEN + timeFormatted);
            timeScore.setScore(11);

            int borderShrinkTimeConfig = plugin.getConfig().getInt("border.start-time");
            int gameTimeConfig = plugin.getConfig().getInt("game-time");
            int borderShrinkTimeLeft = (timeLeft - gameTimeConfig) + borderShrinkTimeConfig;
            int borderMinutes = borderShrinkTimeLeft / 60;
            int borderSeconds = borderShrinkTimeLeft % 60;
            String borderTimeFormatted = String.format("%02d:%02d", borderMinutes, borderSeconds);
            Score borderShrinkScore = objective.getScore(langHandler.getMessage("game.score-borderShrink") + ChatColor.GREEN + borderTimeFormatted);
            if (borderShrinkTimeLeft >= 0) {
                borderShrinkScore.setScore(10);
            }

            int pvpTimeConfig = plugin.getConfig().getInt("grace-period");
            int pvpTimeLeft = (timeLeft - gameTimeConfig) + pvpTimeConfig;
            int pvpMinutes = pvpTimeLeft / 60;
            int pvpSeconds = pvpTimeLeft % 60;
            String pvpTimeFormatted = String.format("%02d:%02d", pvpMinutes, pvpSeconds);
            Score pvpScore = objective.getScore(langHandler.getMessage("game.score-pvp") + ChatColor.GREEN + pvpTimeFormatted);
            if (pvpTimeLeft >= 0) {
                pvpScore.setScore(9);
            }

            objective.getScore("").setScore(8);

            int chestRefillInterval = plugin.getConfig().getInt("chestrefill.interval");
            int chestRefillTimeLeft = timeLeft % chestRefillInterval;
            int chestMinutes = chestRefillTimeLeft / 60;
            int chestSeconds = chestRefillTimeLeft % 60;
            String chestTimeFormatted = String.format("%02d:%02d", chestMinutes, chestSeconds);
            Score chestRefillScore = objective.getScore(langHandler.getMessage("game.score-chestrefill") + ChatColor.GREEN + chestTimeFormatted);
            if (chestRefillTimeLeft >= 0) {
                chestRefillScore.setScore(7);
            }

            int supplyDropInterval = plugin.getConfig().getInt("supplydrop.interval");
            int supplyDropTimeLeft = timeLeft % supplyDropInterval;
            Score supplyDropScore = objective.getScore(langHandler.getMessage("game.score-supplydrop") + ChatColor.GREEN + supplyDropTimeLeft);
            if (supplyDropTimeLeft >= 0) {
                supplyDropScore.setScore(6);
            }

            if (plugin.getConfig().getInt("players-per-team") > 1) {
                for (List<Player> team : teams) {
                    if (team.contains(player)) {
                        for (Player teamMember : team) {
                            if (!teamMember.equals(player)) {
                                String teammateName = teamMember.getName();
                                ChatColor color = playersAlive.contains(teamMember) ? ChatColor.GREEN : ChatColor.RED;
                                String scoreName = langHandler.getMessage("game.score-teammate") + color + teammateName;
                                Score teammateScore = objective.getScore(scoreName);
                                teammateScore.setScore(5);
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
