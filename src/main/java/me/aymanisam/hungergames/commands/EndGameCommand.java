package me.aymanisam.hungergames.commands;

import com.github.retrooper.packetevents.protocol.world.states.enums.South;
import me.aymanisam.hungergames.HungerGames;
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

import java.util.Objects;

import static me.aymanisam.hungergames.HungerGames.gameStarted;
import static me.aymanisam.hungergames.HungerGames.gameStarting;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teamsAlive;
import static me.aymanisam.hungergames.listeners.TeamVotingListener.giveVotingBook;

public class EndGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;
    private final CountDownHandler countDownHandler;
    private final SetSpawnHandler setSpawnHandler;

    public EndGameCommand(HungerGames plugin, LangHandler langHandler, GameSequenceHandler gameSequenceHandler, CountDownHandler countDownHandler, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.gameSequenceHandler = gameSequenceHandler;
        this.countDownHandler = countDownHandler;
        this.setSpawnHandler = setSpawnHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (!(player.hasPermission("hungergames.end"))) {
                sender.sendMessage(langHandler.getMessage(player, "no-permission"));
                return true;
            }
            ;
        }

        if (!gameStarted && !gameStarting) {
            sender.sendMessage(langHandler.getMessage(sender instanceof Player ? (Player) sender : null, "game.not-started"));
            return true;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ;
            player.sendTitle("", langHandler.getMessage(player, "game.ended"), 5, 20, 10);
        }

        if (gameStarting) {
            countDownHandler.cancelCountDown();
            playersAlive.clear();
            gameStarting = false;
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (setSpawnHandler.spawnPointMap.containsValue(player)) {
                    giveVotingBook(player, langHandler);
                    Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20.0);
                }
            }
            return true;
        }

        gameSequenceHandler.endGame();

        return true;
    }
}
