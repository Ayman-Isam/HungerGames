package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.listeners.TeamVotingListener;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class CountDownHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;
    private final TeamsHandler teamsHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final TeamVotingListener teamVotingListener;

    public CountDownHandler(HungerGames plugin, SetSpawnHandler setSpawnHandler, GameSequenceHandler gameSequenceHandler, TeamVotingListener teamVotingListener) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.gameSequenceHandler = gameSequenceHandler;
        this.teamsHandler = new TeamsHandler(plugin);
        this.setSpawnHandler = setSpawnHandler;
        this.teamVotingListener = teamVotingListener;
    }

    public void startCountDown() {
        if (plugin.getConfig().getBoolean("voting")) {
            String highestVotedGameMode;
            int teamSize;

            if (teamVotingListener.votedSolo >= teamVotingListener.votedDuo && teamVotingListener.votedSolo >= teamVotingListener.votedTrio) {
                highestVotedGameMode = langHandler.getMessage("game.solo-inv");
                teamSize = 1;
            } else if (teamVotingListener.votedDuo >= teamVotingListener.votedTrio) {
                highestVotedGameMode = langHandler.getMessage("game.duo-inv");
                teamSize = 2;
            } else {
                highestVotedGameMode = langHandler.getMessage("game.trio-inv");
                teamSize = 3;
            }

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle("", langHandler.getMessage("game.voted-highest") + highestVotedGameMode, 5, 20, 10);
            }

            plugin.reloadConfig();
            plugin.getConfig().set("players-per-team", teamSize);
            plugin.saveConfig();

            plugin.getServer().getScheduler().runTaskLater(plugin, this::runAfterDelay, 20L * 5);
        }
    }

    private void runAfterDelay() {
        playersAlive.addAll(setSpawnHandler.spawnPointMap.values());

        teamsHandler.createTeam();

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            this.gameSequenceHandler.startGame();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
            HungerGames.gameStarting = false;
        }, 20L * 20);

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
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                langHandler.getLangConfig(player);
                String message = langHandler.getMessage(messageKey);
                player.sendTitle("", message, 5, 20, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, delayInTicks);
    }
}
