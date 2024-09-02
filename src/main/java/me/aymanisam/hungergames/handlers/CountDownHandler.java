package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.listeners.TeamVotingListener;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class CountDownHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;
    private final TeamsHandler teamsHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final TeamVotingListener teamVotingListener;
    private final ConfigHandler configHandler;
    private final List<BukkitTask> countDownTasks = new ArrayList<>();

    public static int playersPerTeam;

    public CountDownHandler(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, GameSequenceHandler gameSequenceHandler, TeamVotingListener teamVotingListener, ScoreBoardHandler scoreBoardHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.gameSequenceHandler = gameSequenceHandler;
        this.teamsHandler = new TeamsHandler(plugin, langHandler, scoreBoardHandler);
        this.setSpawnHandler = setSpawnHandler;
        this.teamVotingListener = teamVotingListener;
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    public void startCountDown(World world) {

        playersPerTeam = configHandler.getWorldConfig(world).getInt("players-per-team");

        if (configHandler.getWorldConfig(world).getBoolean("voting")) {
            String highestVotedGameMode;
            int teamSize;

            int votedSolo = Collections.frequency(TeamVotingListener.playerVotes.values(), "solo");
            int votedDuo = Collections.frequency(TeamVotingListener.playerVotes.values(), "duo");
            int votedTrio = Collections.frequency(TeamVotingListener.playerVotes.values(), "trio");
            int votedVersus = Collections.frequency(TeamVotingListener.playerVotes.values(), "versus");

            if (votedSolo >= votedDuo && votedSolo >= votedTrio && votedSolo >= votedVersus) {
                highestVotedGameMode = langHandler.getMessage(null, "team.solo-inv");
                teamSize = 1;
            } else if (votedDuo >= votedTrio && votedDuo >= votedVersus) {
                highestVotedGameMode = langHandler.getMessage(null, "team.duo-inv");
                teamSize = 2;
            } else if (votedTrio >= votedVersus) {
                highestVotedGameMode = langHandler.getMessage(null, "team.trio-inv");
                teamSize = 3;
            } else {
                highestVotedGameMode = langHandler.getMessage(null, "team.versus-inv");
                teamSize = 0;
            }

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(langHandler.getMessage(player, "team.voted-highest", highestVotedGameMode));
                player.sendTitle("", langHandler.getMessage(player, "team.voted-highest", highestVotedGameMode), 5, 40, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
                teamVotingListener.closeVotingInventory(player);
            }

            configHandler.createWorldConfig(world);
            configHandler.getWorldConfig(world).set("players-per-team", teamSize);
            configHandler.saveWorldConfig(world);

            playersPerTeam = teamSize;

            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> runAfterDelay(world), 20L * 5);
            countDownTasks.add(task);
        } else {
            BukkitTask task = plugin.getServer().getScheduler().runTask(plugin, () -> runAfterDelay(world));
            countDownTasks.add(task);
        }
    }

    private void runAfterDelay(World world) {
        playersAlive.addAll(setSpawnHandler.spawnPointMap.values());

        teamsHandler.createTeam();

        countDownTasks.add(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            this.gameSequenceHandler.startGame(world);
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
            HungerGames.gameStarting = false;
        }, 20L * 20));

        countDown("startgame.20-s", 0L);
        countDown("startgame.15-s", 20L * 5);
        countDown("startgame.10-s", 20L * 10);
        countDown("startgame.5-s", 20L * 15);
        countDown("startgame.4-s", 20L * 16);
        countDown("startgame.3-s", 20L * 17);
        countDown("startgame.2-s", 20L * 18);
        countDown("startgame.1-s", 20L * 19);
    }

    private void countDown(String messageKey, long delayInTicks) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                String message = langHandler.getMessage(player, messageKey);
                player.sendTitle("", message, 5, 20, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, delayInTicks);
        countDownTasks.add(task);
    }

    public void cancelCountDown() {
        for (BukkitTask task : countDownTasks) {
            task.cancel();
        }
        countDownTasks.clear();
    }
}
