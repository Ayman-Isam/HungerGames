package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
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

import static me.aymanisam.hungergames.HungerGames.*;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;
import static me.aymanisam.hungergames.listeners.TeamVotingListener.giveVotingBook;

public class EndGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;
    private final CountDownHandler countDownHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ConfigHandler configHandler;

    public EndGameCommand(HungerGames plugin, LangHandler langHandler, GameSequenceHandler gameSequenceHandler, CountDownHandler countDownHandler, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.gameSequenceHandler = gameSequenceHandler;
        this.countDownHandler = countDownHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(gameStarted.getOrDefault(p.getWorld(), false)) && !(gameStarting.getOrDefault(p.getWorld(), false))) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "game.not-started"));
            return true;
        }

        for (Player player : (p.getWorld().getPlayers())) {
            player.sendTitle("", langHandler.getMessage(player, "game.ended"), 5, 20, 10);
        }

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.get(p.getWorld());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(p.getWorld(), k -> new ArrayList<>());

        if (gameStarting.getOrDefault(p.getWorld(), false)) {
            countDownHandler.cancelCountDown(p.getWorld());
            worldPlayersAlive.clear();
            gameStarting.put(p.getWorld(), false);
            for (Player player : p.getWorld().getPlayers()) {
                if (worldSpawnPointMap.containsValue(player)) {
                    giveVotingBook(player, langHandler);
                    Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20.0);
                }
            }
            return true;
        }

        gameSequenceHandler.endGame(false, p.getWorld());

        return true;
    }
}
