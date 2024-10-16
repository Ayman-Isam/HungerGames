package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.listeners.TeamVotingListener;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class CountDownHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;
    private final TeamsHandler teamsHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final TeamVotingListener teamVotingListener;
    private final ConfigHandler configHandler;
    private final Map<World, List<BukkitTask>> countDownTasks = new HashMap<>();

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
        List<BukkitTask> worldCountDownTasks = countDownTasks.computeIfAbsent(world, k -> new ArrayList<>());

        if (configHandler.getWorldConfig(world).getBoolean("voting")) {
            String highestVotedGameMode;
            int teamSize;

            Map<Player, String> worldPlayerVotes = TeamVotingListener.playerVotes.computeIfAbsent(world, k -> new HashMap<>());

            int votedSolo = (int) worldPlayerVotes.values().stream().filter("solo"::equals).count();
            int votedDuo = (int) worldPlayerVotes.values().stream().filter("duo"::equals).count();
            int votedTrio = (int) worldPlayerVotes.values().stream().filter("trio"::equals).count();
            int votedVersus = (int) worldPlayerVotes.values().stream().filter("versus"::equals).count();

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

            for (Player player : world.getPlayers()) {
                player.sendMessage(langHandler.getMessage(player, "team.voted-highest", highestVotedGameMode));
                player.sendTitle("", langHandler.getMessage(player, "team.voted-highest", highestVotedGameMode), 5, 40, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
                teamVotingListener.closeVotingInventory(player);
            }

            configHandler.createWorldConfig(world);
            configHandler.getWorldConfig(world).set("players-per-team", teamSize);
            configHandler.saveWorldConfig(world);


            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> runAfterDelay(world), 20L * 5);
            worldCountDownTasks.add(task);
        } else {
            BukkitTask task = plugin.getServer().getScheduler().runTask(plugin, () -> runAfterDelay(world));
            worldCountDownTasks.add(task);
        }

        playersPerTeam = configHandler.getWorldConfig(world).getInt("players-per-team");

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(world, k -> new HashMap<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());

        worldPlayersAlive.addAll(worldSpawnPointMap.values());
    }

    private void runAfterDelay(World world) {
        List<BukkitTask> worldCountDownTasks = countDownTasks.computeIfAbsent(world, k -> new ArrayList<>());

        teamsHandler.createTeam(world);

        worldCountDownTasks.add(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            this.gameSequenceHandler.startGame(world);
            for (Player player : world.getPlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
        }, 20L * 20));

        countDown("startgame.20-s", 0L, world);
        countDown("startgame.15-s", 20L * 5, world);
        countDown("startgame.10-s", 20L * 10, world);
        countDown("startgame.5-s", 20L * 15, world);
        countDown("startgame.4-s", 20L * 16, world);
        countDown("startgame.3-s", 20L * 17, world);
        countDown("startgame.2-s", 20L * 18, world);
        countDown("startgame.1-s", 20L * 19, world);
    }

    private void countDown(String messageKey, long delayInTicks, World world) {
        List<BukkitTask> worldCountDownTasks = countDownTasks.computeIfAbsent(world, k -> new ArrayList<>());

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : world.getPlayers()) {
                String message = langHandler.getMessage(player, messageKey);
                player.sendTitle("", message, 5, 20, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, delayInTicks);
        worldCountDownTasks.add(task);
    }

    public void cancelCountDown(World world) {
        List<BukkitTask> worldCountDownTasks = countDownTasks.computeIfAbsent(world, k -> new ArrayList<>());

        for (BukkitTask task : worldCountDownTasks) {
            task.cancel();
        }
        worldCountDownTasks.clear();
    }
}
