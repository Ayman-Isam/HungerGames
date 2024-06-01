package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ArenaHandler arenaHandler;
    private final CountDownHandler countDownHandler;

    public StartGameCommand(HungerGames plugin, SetSpawnHandler setSpawnHandler, GameSequenceHandler gameSequenceHandler) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.setSpawnHandler = setSpawnHandler;
        this.arenaHandler = new ArenaHandler(plugin);
        this.countDownHandler = new CountDownHandler(plugin, setSpawnHandler, gameSequenceHandler);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            langHandler.getLangConfig(player);
        }

        if (!(sender.hasPermission("hungergames.start") || sender instanceof Player)) {
            sender.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        if (HungerGames.gameStarted) {
            sender.sendMessage(langHandler.getMessage("startgame.started"));
            return true;
        }

        if (HungerGames.gameStarting) {
            sender.sendMessage(langHandler.getMessage("startgame.starting"));
            return true;
        }

        String world = arenaHandler.getArenaConfig().getString("region.world");

        if (world == null) {
            sender.sendMessage(langHandler.getMessage("startgame.set-arena"));
            return true;
        }

        if (setSpawnHandler.spawnPoints.isEmpty()) {
            sender.sendMessage(langHandler.getMessage("startgame.set-spawn"));
            return true;
        }

        if (setSpawnHandler.spawnPointMap.size() < plugin.getConfig().getInt("min-players")) {
            String message = String.format(langHandler.getMessage("startgame.min-players"), plugin.getConfig().getInt("min-players"));
            sender.sendMessage(message);
            return true;
        }

        HungerGames.gameStarting = true;

        countDownHandler.startCountDown();

        return true;
    }
}
