package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JoinGameCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;

    public JoinGameCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler) {
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(player.hasPermission("hungergames.join"))) {
            sender.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (HungerGames.gameStarted) {
            player.sendMessage(langHandler.getMessage(player, "startgame.started"));
            return true;
        }

        if (HungerGames.gameStarting) {
            player.sendMessage(langHandler.getMessage(player, "startgame.starting"));
            return true;
        }

        if (setSpawnHandler.spawnPointMap.containsValue(player)) {
            player.sendMessage(langHandler.getMessage(player, "game.already-joined"));
            return true;
        }

        setSpawnHandler.createSetSpawnConfig(player.getWorld());

        if (setSpawnHandler.spawnPoints.size() <= setSpawnHandler.spawnPointMap.size()) {
            player.sendMessage(langHandler.getMessage(player, "game.join-fail"));
            return true;
        }

        setSpawnHandler.teleportPlayerToSpawnpoint(player);

        return true;
    }
}
