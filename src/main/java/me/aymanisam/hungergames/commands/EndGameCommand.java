package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.GameSequenceHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;

    public EndGameCommand(HungerGames plugin, SetSpawnHandler setSpawnHandler, GameSequenceHandler gameSequenceHandler) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.gameSequenceHandler = gameSequenceHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            langHandler.loadLanguageConfig(player);
        }

        if (!(sender.hasPermission("hungergames.end") && sender instanceof Player)) {
            sender.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        if (!HungerGames.gameStarted) {
            sender.sendMessage(langHandler.getMessage("endgame.not-started"));
            return true;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            langHandler.loadLanguageConfig(player);
            player.sendTitle("", langHandler.getMessage("endgame.ended"), 5, 20, 10);
        }

        gameSequenceHandler.endGame();

        return true;
    }
}
