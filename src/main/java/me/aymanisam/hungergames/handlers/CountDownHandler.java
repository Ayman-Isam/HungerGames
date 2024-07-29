package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.listeners.TeamVotingListener;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.aymanisam.hungergames.HungerGames.gameWorld;
import static me.aymanisam.hungergames.HungerGames.gameWorld;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

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

    public CountDownHandler(HungerGames plugin, SetSpawnHandler setSpawnHandler, GameSequenceHandler gameSequenceHandler, TeamVotingListener teamVotingListener, ScoreBoardHandler scoreBoardHandler) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.gameSequenceHandler = gameSequenceHandler;
        this.teamsHandler = new TeamsHandler(plugin, scoreBoardHandler);
        this.setSpawnHandler = setSpawnHandler;
        this.teamVotingListener = teamVotingListener;
        this.configHandler = new ConfigHandler(plugin);
    }

    public void startCountDown() {

        playersPerTeam = configHandler.getWorldConfig(gameWorld).getInt("players-per-team");

        if (configHandler.getWorldConfig(gameWorld).getBoolean("voting")) {
            String highestVotedGameMode;
            int teamSize;

            int votedSolo = Collections.frequency(TeamVotingListener.playerVotes.values(), "solo");
            int votedDuo = Collections.frequency(TeamVotingListener.playerVotes.values(), "duo");
            int votedTrio = Collections.frequency(TeamVotingListener.playerVotes.values(), "trio");
            int votedVersus = Collections.frequency(TeamVotingListener.playerVotes.values(), "versus");

            if (votedSolo >= votedDuo && votedSolo >= votedTrio && votedSolo >= votedVersus) {
                highestVotedGameMode = langHandler.getMessage("team.solo-inv");
                teamSize = 1;
            } else if (votedDuo >= votedTrio && votedDuo >= votedVersus) {
                highestVotedGameMode = langHandler.getMessage("team.duo-inv");
                teamSize = 2;
            } else if (votedTrio >= votedVersus) {
                highestVotedGameMode = langHandler.getMessage("team.trio-inv");
                teamSize = 3;
            } else {
                highestVotedGameMode = langHandler.getMessage("team.versus-inv");
                teamSize = 0;
            }

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(langHandler.getMessage("team.voted-highest", highestVotedGameMode));
                player.sendTitle("", langHandler.getMessage("team.voted-highest", highestVotedGameMode), 5, 40, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
                teamVotingListener.closeVotingInventory(player);
            }

            configHandler.createWorldConfig(gameWorld);
            configHandler.getWorldConfig(gameWorld).set("players-per-team", teamSize);
            configHandler.saveWorldConfig(gameWorld);

            playersPerTeam = teamSize;

            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, this::runAfterDelay, 20L * 5);
            countDownTasks.add(task);
        }
    }

    private void runAfterDelay() {
        playersAlive.addAll(setSpawnHandler.spawnPointMap.values());

        teamsHandler.createTeam();

        countDownTasks.add(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            this.gameSequenceHandler.startGame();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
            HungerGames.gameStarting = false;
        }, 20L * 20));

        countDown("startgame.20-s", 20L * 0);
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
                langHandler.getLangConfig(player);
                String message = langHandler.getMessage(messageKey);
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
