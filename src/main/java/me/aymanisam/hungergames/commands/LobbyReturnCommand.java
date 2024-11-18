package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
import me.aymanisam.hungergames.listeners.SignClickListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static me.aymanisam.hungergames.HungerGames.*;
import static me.aymanisam.hungergames.HungerGames.totalTimeSpent;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.*;
import static me.aymanisam.hungergames.listeners.TeamVotingListener.playerVotes;

public class LobbyReturnCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final SignClickListener signClickListener;
    private final ConfigHandler configHandler;
    private final SignHandler signHandler;
    private final CountDownHandler countDownHandler;
    private final ResetPlayerHandler resetPlayerHandler;
    private final ScoreBoardHandler scoreBoardHandler;

    public LobbyReturnCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, ArenaHandler arenaHandler, CountDownHandler countDownHandler, ScoreBoardHandler scoreBoardHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.configHandler = plugin.getConfigHandler();
        this.signClickListener = new SignClickListener(plugin, langHandler, setSpawnHandler, arenaHandler);
        this.signHandler = new SignHandler(plugin);
        this.countDownHandler = countDownHandler;
        this.resetPlayerHandler = new ResetPlayerHandler();
        this.scoreBoardHandler = scoreBoardHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(player.hasPermission("hungergames.lobby"))) {
            sender.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        String lobbyWorldName = (String) configHandler.createPluginSettings().get("lobby-world");

        World world = player.getWorld();

        if (world.getName().equals(lobbyWorldName)) {
            player.sendMessage(langHandler.getMessage(player, "game.not-lobby"));
            return true;
        }

        setSpawnHandler.removePlayerFromSpawnPoint(player, world);

        List<Player> worldPlayersWaiting = setSpawnHandler.playersWaiting.computeIfAbsent(world, k -> new ArrayList<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());
        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(world, k -> new HashMap<>());
        List<String> worldSpawnPoints = setSpawnHandler.spawnPoints.computeIfAbsent(world, k -> new ArrayList<>());
        List<Player> worldPlayersPlacement = playerPlacements.computeIfAbsent(player.getWorld(), k -> new ArrayList<>());

        for (Player p : world.getPlayers()) {
            langHandler.getLangConfig(p);
            p.sendMessage(langHandler.getMessage(player, "game.left", player.getName(), worldSpawnPointMap.size(), worldSpawnPoints.size()));
        }

        int minPlayers = configHandler.getWorldConfig(world).getInt("min-players");

        if (worldSpawnPointMap.size() < minPlayers) {
            if (gameStarting.getOrDefault(world, false)) {
                countDownHandler.cancelCountDown(world);
                for (Player p : world.getPlayers()) {
                    p.sendMessage(langHandler.getMessage(p, "startgame.cancelled"));
                }
            }
            gameStarting.put(world, false);
        }

        assert lobbyWorldName != null;
        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);

        if (lobbyWorld != null) {
            player.teleport(lobbyWorld.getSpawnLocation());
        } else {
            plugin.getLogger().log(Level.SEVERE, "Could not find lobbyWorld [ " + lobbyWorldName + "]");
        }

        int timeAlive = configHandler.getWorldConfig(world).getInt("game-time") - timeLeft.getOrDefault(world, 0);
        Long timeSpent = totalTimeSpent.getOrDefault(player, 0L);
        totalTimeSpent.put(player, timeAlive + timeSpent);

        resetPlayerHandler.resetPlayer(player);
        Map<Player, BossBar> worldPlayerBossBar = playerBossBars.computeIfAbsent(world, k -> new HashMap<>());

        BossBar bossBar = worldPlayerBossBar.get(player);

        if (bossBar != null) {
            bossBar.removePlayer(player);
            worldPlayerBossBar.remove(player);
            bossBar.setVisible(false);
        }

        Map<Player, String> worldPlayerVotes = playerVotes.computeIfAbsent(world, k -> new HashMap<>());
        worldPlayerVotes.remove(player);

        scoreBoardHandler.removeScoreboard(player);

        if (isGameStartingOrStarted(world)) {
            worldPlayersAlive.remove(player);
            if (configHandler.getWorldConfig(world).getInt("players-per-team") == 1) {
                worldPlayersPlacement.add(player);
            }
        } else {
            setSpawnHandler.removePlayerFromSpawnPoint(player, world);
            worldPlayersWaiting.remove(player);
        }

        signClickListener.setSignContent(signHandler.loadSignLocations());

        return true;
    }
}
