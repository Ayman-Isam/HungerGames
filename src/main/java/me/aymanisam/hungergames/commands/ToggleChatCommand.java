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

    public ToggleChatCommand(HungerGames plugin, TeamsHandler teamsHandler){
        this.langHandler = new LangHandler(plugin);
        this.configHandler = new ConfigHandler(plugin);
        this.teamsHandler = teamsHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
            return true;
        }

        langHandler.getLangConfig(player);

        if (!player.hasPermission("hungergames.teamchat")) {
            player.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        if (!gameStarted && !gameStarting) {
            player.sendMessage(langHandler.getMessage("game.not-started"));
            return true;
        }

        if (configHandler.getWorldConfig(gameWorld).getInt("players-per-team") == 1) {
            player.sendMessage(langHandler.getMessage("game.not-team"));
            return true;
        }

        boolean currentMode = teamsHandler.isChatModeEnabled(player);
        playerChatModes.put(player, !currentMode);


        if (!currentMode) {
            player.sendMessage(langHandler.getMessage("team.chat-enabled"));
        } else {
            player.sendMessage(langHandler.getMessage("team.chat-disabled"));
        }

        return true;
    }
}
