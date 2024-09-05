package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.HungerGames.gameStarted;
import static me.aymanisam.hungergames.HungerGames.gameStarting;

public class StartGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ArenaHandler arenaHandler;
    private final CountDownHandler countDownHandler;
    private final ConfigHandler configHandler;

    public StartGameCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, CountDownHandler countDownHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
        this.countDownHandler = countDownHandler;
        this.configHandler = new ConfigHandler(plugin, langHandler);
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

        if (gameStarted.getOrDefault(player.getWorld(), false)) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.started"));
            return true;
        }

        if (gameStarting.getOrDefault(player.getWorld(), false)) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.starting"));
            return true;
        }

        String world = arenaHandler.getArenaConfig(player.getWorld()).getString("region.world");

        if (world == null) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.set-arena"));
            return true;
        }

        List<String> worldSpawnPoints = setSpawnHandler.spawnPoints.computeIfAbsent(player.getWorld(), k -> new ArrayList<>());

        if (worldSpawnPoints.isEmpty()) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.set-spawn"));
            return true;
        }

        int minPlayers = configHandler.getWorldConfig(player.getWorld()).getInt("min-players");

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(player.getWorld(), k -> new HashMap<>());

        if (worldSpawnPointMap.size() < minPlayers) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.min-players", minPlayers));
            return true;
        }

        gameStarting.put(player.getWorld(), false);

        countDownHandler.startCountDown(player.getWorld());

        return true;
    }
}
