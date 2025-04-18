package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.listeners.TeamVotingListener;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static me.aymanisam.hungergames.HungerGames.customTeams;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.*;
import static me.aymanisam.hungergames.handlers.SetSpawnHandler.autoStartTasks;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;
import static me.aymanisam.hungergames.listeners.TeamVotingListener.playerVotes;

public class CountDownHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;
    private final TeamsHandler teamsHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final TeamVotingListener teamVotingListener;
    private final ConfigHandler configHandler;
    private final Map<String, List<BukkitTask>> countDownTasks = new HashMap<>();

    public static int playersPerTeam;

    public CountDownHandler(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, GameSequenceHandler gameSequenceHandler, TeamVotingListener teamVotingListener) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.gameSequenceHandler = gameSequenceHandler;
        this.teamsHandler = new TeamsHandler(plugin, langHandler);
        this.setSpawnHandler = setSpawnHandler;
        this.teamVotingListener = teamVotingListener;
        this.configHandler = plugin.getConfigHandler();
    }

    public void startCountDown(World world) {
        List<BukkitTask> worldCountDownTasks = countDownTasks.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        List<BukkitTask> worldAutoStartTasks = autoStartTasks.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        for (BukkitTask task : worldAutoStartTasks) {
            task.cancel();
        }
        worldAutoStartTasks.clear();

        if (configHandler.getWorldConfig(world).getBoolean("voting")) {
            String highestVotedGameMode;
            int teamSize;

            Map<Player, String> worldPlayerVotes = TeamVotingListener.playerVotes.computeIfAbsent(world.getName(), k -> new HashMap<>());

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

        int maxTeamSize = customTeams.values().stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
        plugin.getConfigHandler().getWorldConfig(world).set("players-per-team", maxTeamSize);
        plugin.getConfigHandler().saveWorldConfig(world);

        playersPerTeam = configHandler.getWorldConfig(world).getInt("players-per-team");

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(world.getName(), k -> new HashMap<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        worldPlayersAlive.addAll(worldSpawnPointMap.values());
    }

    private void runAfterDelay(World world) {
        List<BukkitTask> worldCountDownTasks = countDownTasks.computeIfAbsent(world.getName(), k -> new ArrayList<>());

	    teamsHandler.createTeam(world, configHandler.getPluginSettings().getBoolean("custom-teams"));

        int countDownDuration = configHandler.getWorldConfig(world).getInt("countdown");

        worldCountDownTasks.add(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            this.gameSequenceHandler.startGame(world);
            for (Player player : world.getPlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
        }, countDownDuration * 20L));

        int intervals = (countDownDuration - 5) / 5;

        countDown("startgame.start-s", countDownDuration, world);

        for (int i = 0; i < intervals; i++) {
            countDown("startgame.mid-s", countDownDuration - i * 5, world);
        }

        countDown("startgame.mid-s", 5, world);
        countDown("startgame.mid-s", 4, world);
        countDown("startgame.mid-s", 3, world);
        countDown("startgame.mid-s", 2, world);
        countDown("startgame.end-s", 1, world);
    }

    private void countDown(String messageKey, long timeLeftSeconds, World world) {
        List<BukkitTask> worldCountDownTasks = countDownTasks.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        int countDownDuration = configHandler.getWorldConfig(world).getInt("countdown");

        if (countDownDuration < 5) {
            return;
        }

        long delayInTicks = (countDownDuration - timeLeftSeconds) * 20L;

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : world.getPlayers()) {
                String message = langHandler.getMessage(player, messageKey, timeLeftSeconds);
                player.sendTitle("", message, 5, 20, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, delayInTicks);
        worldCountDownTasks.add(task);
    }

    public void cancelCountDown(World world) {
        List<BukkitTask> worldCountDownTasks = countDownTasks.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        List<BukkitTask> worldAutoStartTasks = autoStartTasks.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        for (BukkitTask task : worldCountDownTasks) {
            task.cancel();
        }
        worldCountDownTasks.clear();

        for (BukkitTask task : worldAutoStartTasks) {
            task.cancel();
        }
        worldAutoStartTasks.clear();

        for (Player player: world.getPlayers()) {
            removeBossBar(player);
        }

        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        List<Player> worldStartingPlayers = startingPlayers.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        Map<Player, String> worldPlayerVotes = playerVotes.computeIfAbsent(world.getName(), k -> new HashMap<>());

        worldPlayersAlive.clear();
        worldStartingPlayers.clear();
        worldPlayerVotes.clear();
    }
}
