package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SpectatePlayerHandler;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class SpectatePlayerCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SpectatePlayerHandler spectatePlayerHandler;

    public SpectatePlayerCommand(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.spectatePlayerHandler = new SpectatePlayerHandler(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
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

        return true;
    }
}
