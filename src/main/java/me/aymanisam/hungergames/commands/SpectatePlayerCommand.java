package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SpectatePlayerHandler;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class SpectatePlayerCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final SpectatePlayerHandler spectatePlayerHandler;

    public SpectatePlayerCommand(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
        this.spectatePlayerHandler = new SpectatePlayerHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(player.hasPermission("hungergames.spectate"))) {
            sender.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (!(player.getGameMode() == GameMode.SPECTATOR)) {
            player.sendMessage(langHandler.getMessage(player, "spectate.not-spectator"));
            return true;
        }

        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(player.getWorld(), k -> new ArrayList<>());

        if (worldPlayersAlive.isEmpty()) {
            player.sendMessage(langHandler.getMessage(player, "spectate.no-player"));
            return true;
        }

        spectatePlayerHandler.openSpectatorGUI(player);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        return true;
    }
}
