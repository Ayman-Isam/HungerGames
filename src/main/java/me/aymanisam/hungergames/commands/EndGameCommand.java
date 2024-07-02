package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.GameSequenceHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EndGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;

    public EndGameCommand(HungerGames plugin, GameSequenceHandler gameSequenceHandler) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.gameSequenceHandler = gameSequenceHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (!(player.hasPermission("hungergames.end"))) {
                sender.sendMessage(langHandler.getMessage("no-permission"));
                return true;
            }
            langHandler.getLangConfig(player);
        }

        if (!HungerGames.gameStarted) {
            sender.sendMessage(langHandler.getMessage("game.not-started"));
            return true;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            langHandler.getLangConfig(player);
            player.sendTitle("", langHandler.getMessage("game.ended"), 5, 20, 10);
        }

        gameSequenceHandler.endGame();

        return true;
    }
}
