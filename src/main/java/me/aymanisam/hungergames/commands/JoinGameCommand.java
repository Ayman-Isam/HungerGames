package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinGameCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;

    public JoinGameCommand(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.langHandler = new LangHandler(plugin);
        this.setSpawnHandler = setSpawnHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
            return true;
        }

        langHandler.getLangConfig(player);

        if (!(player.hasPermission("hungergames.join"))) {
            sender.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        if (HungerGames.gameStarted) {
            player.sendMessage(langHandler.getMessage("startgame.started"));
            return true;
        }

        if (setSpawnHandler.spawnPointMap.containsValue(player)) {
            player.sendMessage(langHandler.getMessage("join.already-joined"));
            return true;
        }

        if (setSpawnHandler.spawnPoints.size() <= setSpawnHandler.spawnPointMap.size()) {
            player.sendMessage(langHandler.getMessage("join.join-fail"));
            return true;
        }

        setSpawnHandler.teleportPlayerToSpawnpoint(player);

        return true;
    }
}
