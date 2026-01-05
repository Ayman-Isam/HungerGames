package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.CountDownHandler;
import me.aymanisam.hungergames.handlers.GameSequenceHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.ResetPlayerHandler;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.*;

public class EndGameCommand implements CommandExecutor {
    private final HungerGames plugin;
	private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;
    private final CountDownHandler countDownHandler;
    private final ResetPlayerHandler resetPlayerHandler;

	public EndGameCommand(HungerGames plugin, LangHandler langHandler, GameSequenceHandler gameSequenceHandler, CountDownHandler countDownHandler) {
		this.plugin = plugin;
		this.langHandler = langHandler;
        this.gameSequenceHandler = gameSequenceHandler;
        this.countDownHandler = countDownHandler;
        this.resetPlayerHandler = new ResetPlayerHandler();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player player = null;

        if (sender instanceof Player) {
            player = ((Player) sender);
        }

        if (player != null && !player.hasPermission("hungergames.end")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        World world;

        if (player == null) {
            if (args.length != 1) {
                sender.sendMessage(langHandler.getMessage(null, "no-world"));
                return true;
            }
            String worldName = args[0];
            if (!hgWorldNames.contains(worldName)) {
                sender.sendMessage(langHandler.getMessage(null, "teleport.invalid-world", args[0]));
                plugin.getLogger().info("Loaded maps:" + plugin.getServer().getWorlds().stream().map(World::getName).collect(Collectors.joining(", ")));
                return true;
            }
            world = plugin.getServer().getWorld(worldName);
        } else {
            world = player.getWorld();
        }

	    assert world != null;
	    if (!isGameStartingOrStarted(world.getName())) {
            sender.sendMessage(langHandler.getMessage(player, "game.not-started"));
            return true;
        }

        for (Player onlinePlayer : world.getPlayers()) {
            onlinePlayer.sendTitle("", langHandler.getMessage(onlinePlayer, "game.ended"), 5, 20, 10);
        }

        if (gameStarting.getOrDefault(world.getName(),  false)) {
            countDownHandler.cancelCountDown(world);
            for (Player onlinePlayer : world.getPlayers()) {
                resetPlayerHandler.resetPlayer(onlinePlayer);
            }
        } else {
            gameSequenceHandler.endGame(false, world);
        }

        return true;
    }
}
