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

import java.util.*;

import static me.aymanisam.hungergames.HungerGames.gameStarting;
import static me.aymanisam.hungergames.HungerGames.isGameStartingOrStarted;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.startingPlayers;

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
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!player.hasPermission("hungergames.end")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (!isGameStartingOrStarted(player.getWorld().getName())) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "game.not-started"));
            return true;
        }

        for (Player onlinePlayer : (player.getWorld().getPlayers())) {
            onlinePlayer.sendTitle("", langHandler.getMessage(onlinePlayer, "game.ended"), 5, 20, 10);
        }

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(player.getWorld().getName(), k -> new HashMap<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());
        List<Player> worldStartingPlayers = startingPlayers.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());
        gameStarting.put(player.getWorld().getName(), false);

        if (gameStarting.getOrDefault(player.getWorld().getName(), false)) {
            countDownHandler.cancelCountDown(player.getWorld());
            worldPlayersAlive.clear();
            worldStartingPlayers.clear();

            for (Player p : player.getWorld().getPlayers()) {
                if (worldSpawnPointMap.containsValue(p)) {
                    Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20.0);
                }
            }
            return true;
        }

        gameSequenceHandler.endGame(false, player.getWorld());

        return true;
    }
}
