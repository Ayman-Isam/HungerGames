package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LeaveGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ConfigHandler configHandler;

    public LeaveGameCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(player.hasPermission("hungergames.leave"))) {
            sender.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (!(setSpawnHandler.spawnPointMap.containsValue(player))) {
            player.sendMessage(langHandler.getMessage(player, "game.not-joined"));
            return true;
        }

        setSpawnHandler.removePlayerFromSpawnPoint(player);

        player.teleport(player.getWorld().getSpawnLocation());

        boolean spectating = configHandler.getWorldConfig(player.getWorld()).getBoolean("spectating");
        if (spectating) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            langHandler.getLangConfig(onlinePlayer);
            onlinePlayer.sendMessage(langHandler.getMessage(player, "game.left", player.getName(), setSpawnHandler.spawnPointMap.size(), setSpawnHandler.spawnPoints.size()));
        }

        return true;
    }
}
