package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static me.aymanisam.hungergames.HungerGames.gameStarted;
import static me.aymanisam.hungergames.HungerGames.gameStarting;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.WORLD_BORDER;

public class StartGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ArenaHandler arenaHandler;
    private final CountDownHandler countDownHandler;
    private final ConfigHandler configHandler;
    private final DatabaseHandler databaseHandler;

    public StartGameCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, CountDownHandler countDownHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
        this.countDownHandler = countDownHandler;
        this.configHandler = plugin.getConfigHandler();
        this.databaseHandler = new DatabaseHandler(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!player.hasPermission("hungergames.start")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (gameStarted.getOrDefault(player.getWorld().getName(), false)) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.started"));
            return true;
        }

        if (gameStarting.getOrDefault(player.getWorld().getName(), false)) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.starting"));
            return true;
        }

        String worldName = arenaHandler.getArenaConfig(player.getWorld()).getString("region.world");

        if (worldName == null) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.set-arena"));
            return true;
        }

        List<String> worldSpawnPoints = setSpawnHandler.spawnPoints.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());

        if (worldSpawnPoints.isEmpty()) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.set-spawn"));
            return true;
        }

        int minPlayers = configHandler.getWorldConfig(player.getWorld()).getInt("min-players");

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(player.getWorld().getName(), k -> new HashMap<>());

        if (worldSpawnPointMap.size() < minPlayers) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.min-players", minPlayers));
            return true;
        }

        gameStarting.put(player.getWorld().getName(), true);

        countDownHandler.startCountDown(player.getWorld());

        try {
            PlayerStatsHandler playerStats = databaseHandler.getPlayerStatsFromDatabase(player);

            playerStats.setCredits(playerStats.getCredits() - 1);

            this.plugin.getDatabase().updatePlayerStats(playerStats);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, e.toString());
        }

        return true;
    }
}
