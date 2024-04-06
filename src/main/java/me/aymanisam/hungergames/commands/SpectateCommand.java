package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handler.GameHandler;
import me.aymanisam.hungergames.handler.SpectatorTeleportHandler;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpectateCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final SpectatorTeleportHandler spectatorTeleportHandler;
    private final GameHandler gameHandler;

    public SpectateCommand(HungerGames plugin, GameHandler gameHandler) {
        this.plugin = plugin;
        this.gameHandler = gameHandler;
        this.spectatorTeleportHandler = new SpectatorTeleportHandler(plugin, gameHandler);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender Sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            if (Sender instanceof Player player) {
                plugin.loadLanguageConfig(player);
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    if (!gameHandler.getPlayersAlive().isEmpty()) {
                        spectatorTeleportHandler.openSpectatorGUI(player);
                    } else {
                        Sender.sendMessage(plugin.getMessage("spectate.no-player"));
                    }
                } else {
                    Sender.sendMessage(plugin.getMessage("spectate.not-spectator"));
                }
            } else {
                Sender.sendMessage(plugin.getMessage("spectate.no-server"));
            }
        return true;
    }
}
