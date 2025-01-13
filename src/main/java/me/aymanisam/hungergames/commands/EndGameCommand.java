package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.handlers.CountDownHandler;
import me.aymanisam.hungergames.handlers.GameSequenceHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.aymanisam.hungergames.HungerGames.gameStarting;
import static me.aymanisam.hungergames.HungerGames.isGameStartingOrStarted;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class EndGameCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;
    private final CountDownHandler countDownHandler;
    private final SetSpawnHandler setSpawnHandler;

    public EndGameCommand(LangHandler langHandler, GameSequenceHandler gameSequenceHandler, CountDownHandler countDownHandler, SetSpawnHandler setSpawnHandler) {
        this.langHandler = langHandler;
        this.gameSequenceHandler = gameSequenceHandler;
        this.countDownHandler = countDownHandler;
        this.setSpawnHandler = setSpawnHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!p.hasPermission("hungergames.end")) {
            p.sendMessage(langHandler.getMessage(p, "no-permission"));
            return true;
        }

        if (!isGameStartingOrStarted(p.getWorld().getName())) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "game.not-started"));
            return true;
        }

        for (Player player : (p.getWorld().getPlayers())) {
            player.sendTitle("", langHandler.getMessage(player, "game.ended"), 5, 20, 10);
        }

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.get(p.getWorld().getName());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(p.getWorld().getName(), k -> new ArrayList<>());

        if (gameStarting.getOrDefault(p.getWorld().getName(), false)) {
            countDownHandler.cancelCountDown(p.getWorld());
            worldPlayersAlive.clear();
            gameStarting.put(p.getWorld().getName(), false);
            for (Player player : p.getWorld().getPlayers()) {
                if (worldSpawnPointMap.containsValue(player)) {
                    Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20.0);
                }
            }
            return true;
        }

        gameSequenceHandler.endGame(false, p.getWorld());

        return true;
    }
}
