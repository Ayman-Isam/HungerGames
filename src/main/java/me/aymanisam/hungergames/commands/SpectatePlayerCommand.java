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

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class SpectatePlayerCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final SpectatePlayerHandler spectatePlayerHandler;

    public SpectatePlayerCommand(HungerGames plugin) {
        this.langHandler = new LangHandler(plugin);
        this.spectatePlayerHandler = new SpectatePlayerHandler(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
            return true;
        }

        if (!(player.hasPermission("hungergames.spectate"))) {
            sender.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        if (!(player.getGameMode() == GameMode.SPECTATOR)) {
            player.sendMessage(langHandler.getMessage("spectate.not-spectator"));
            return true;
        }

        if (playersAlive.isEmpty()) {
            player.sendMessage(langHandler.getMessage("spectate.no-player"));
            return true;
        }

        spectatePlayerHandler.openSpectatorGUI(player);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        return true;
    }
}
