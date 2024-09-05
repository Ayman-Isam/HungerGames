package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.TeamsHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static me.aymanisam.hungergames.HungerGames.*;

public class ToggleChatCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;
    private final TeamsHandler teamsHandler;
    public static final Map<Player, Boolean> playerChatModes = new HashMap<>();

    public ToggleChatCommand(HungerGames plugin, LangHandler langHandler, TeamsHandler teamsHandler) {
        this.langHandler = langHandler;
        this.configHandler = new ConfigHandler(plugin, langHandler);
        this.teamsHandler = teamsHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!player.hasPermission("hungergames.teamchat")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (!gameStarted.getOrDefault(player.getWorld(), false) && !gameStarting.getOrDefault(player.getWorld(), false)) {
            player.sendMessage(langHandler.getMessage(player, "game.not-started"));
            return true;
        }

        if (configHandler.getWorldConfig(player.getWorld()).getInt("players-per-team") == 1) {
            player.sendMessage(langHandler.getMessage(player, "game.not-team"));
            return true;
        }

        boolean currentMode = teamsHandler.isChatModeEnabled(player);
        playerChatModes.put(player, !currentMode);


        if (!currentMode) {
            player.sendMessage(langHandler.getMessage(player, "team.chat-enabled"));
        } else {
            player.sendMessage(langHandler.getMessage(player, "team.chat-disabled"));
        }

        return true;
    }
}
