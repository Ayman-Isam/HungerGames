package me.aymanisam.hungergames.handlers;

import fr.mrmicky.fastboard.FastBoard;
import me.aymanisam.hungergames.HungerGames;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

import static me.aymanisam.hungergames.handlers.CountDownHandler.playersPerTeam;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.timeLeft;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;

public class ScoreBoardHandler {
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;

    public static Map<String, Integer> startingPlayers = new HashMap<>();
    public static final Map<UUID, FastBoard> boards = new HashMap<>();

    public ScoreBoardHandler(HungerGames plugin, LangHandler langHandler) {
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

    private String formatScore(Player player, String messageKey, int countdown, int interval) {
        int minutes = countdown / 60;
        int seconds = countdown % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        return langHandler.getMessage(player, messageKey, getColor(interval, countdown) + timeFormatted);
    }

    public void createBoard(Player player) {
        FastBoard board = new FastBoard(player);
        if (playersPerTeam == 1) {
            board.updateTitle(langHandler.getMessage(player, "score.name-solo"));
        } else {
            board.updateTitle(langHandler.getMessage(player, "score.name-team"));
        }

        boards.put(player.getUniqueId(), board);
    }

    public void updateBoard(FastBoard board, World world) {
        FileConfiguration worldConfig = configHandler.getWorldConfig(world);
        int gameTimeConfig = worldConfig.getInt("game-time");
        int borderShrinkTimeConfig = worldConfig.getInt("border.start-time");
        int pvpTimeConfig = worldConfig.getInt("grace-period");
        int chestRefillInterval = worldConfig.getInt("chestrefill.interval");
        int supplyDropInterval = worldConfig.getInt("supplydrop.interval");
        int borderStartSize = worldConfig.getInt("border.size");
        int borderEndSize = worldConfig.getInt("border.final-size");

        int worldTimeLeft = timeLeft.get(world.getName());
        int worldPlayersAliveSize = playersAlive.computeIfAbsent(world.getName(), k -> new ArrayList<>()).size();
        int worldStartingPlayers = startingPlayers.get(world.getName());
        int worldBorderSize = (int) world.getWorldBorder().getSize();
        int borderShrinkTimeLeft = (worldTimeLeft - gameTimeConfig) + borderShrinkTimeConfig;
        int pvpTimeLeft = (worldTimeLeft - gameTimeConfig) + pvpTimeConfig;
        int chestRefillTimeLeft = worldTimeLeft % chestRefillInterval;
        int supplyDropTimeLeft = worldTimeLeft % supplyDropInterval;
        ChatColor borderColor;

        if (borderStartSize == worldBorderSize) {
            borderColor = ChatColor.GREEN;
        } else if (borderEndSize == worldBorderSize) {
            borderColor = ChatColor.RED;
        } else {
            borderColor = ChatColor.YELLOW;
        }

        List<String> lines = new ArrayList<>();

        lines.add("");
        lines.add(langHandler.getMessage(board.getPlayer(), "score.alive", getColor(worldStartingPlayers, worldPlayersAliveSize).toString() + worldPlayersAliveSize));
        lines.add(langHandler.getMessage(board.getPlayer(), "score.border", borderColor.toString() + worldBorderSize));
        lines.add("");
        lines.add(formatScore(board.getPlayer(), "score.time", worldTimeLeft, gameTimeConfig));

        if (borderShrinkTimeLeft >= 0) {
            lines.add(formatScore(board.getPlayer(), "score.borderShrink", borderShrinkTimeLeft, borderShrinkTimeConfig));
        }

        if (pvpTimeLeft >= 0) {
            lines.add(formatScore(board.getPlayer(), "score.pvp", pvpTimeLeft, pvpTimeConfig));
        }

        lines.add("");
        lines.add(formatScore(board.getPlayer(), "score.chestrefill", chestRefillTimeLeft, chestRefillInterval));
        lines.add(formatScore(board.getPlayer(), "score.supplydrop", supplyDropTimeLeft, supplyDropInterval));

        String teamScoreBoard = getScoreBoardTeam(board.getPlayer(), world);

        if (teamScoreBoard != null) {
            lines.add("");
            lines.add(teamScoreBoard);
        }

        board.updateLines(lines);
    }

    private String getScoreBoardTeam(Player player, World world) {
        List<List<Player>> worldTeams = teams.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        if (playersPerTeam > 1) {
            for (List<Player> team : worldTeams) {
                if (team.contains(player)) {
                    for (Player teamMember : team) {
                        if (!teamMember.equals(player)) {
                            String teammateName = teamMember.getName();
                            ChatColor color = worldPlayersAlive.contains(teamMember) ? ChatColor.GREEN : ChatColor.RED;
                            return langHandler.getMessage(player, "score.teammate", color + teammateName);
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    public void removeScoreboard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }
}
