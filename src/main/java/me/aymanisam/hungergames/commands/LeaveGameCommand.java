package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;

    public LeaveGameCommand(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.setSpawnHandler = setSpawnHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
            return true;
        }

        langHandler.loadLanguageConfig(player);

        if (!(player.hasPermission("hungergames.leave"))) {
            sender.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        if (!(setSpawnHandler.spawnPointMap.containsValue(player))) {
            player.sendMessage(langHandler.getMessage("leave.not-joined"));
            return true;
        }

        setSpawnHandler.removePlayerFromSpawnPoint(player);

        player.teleport(player.getWorld().getSpawnLocation());

        boolean spectating = plugin.getConfig().getBoolean("spectating");
        if (spectating) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            langHandler.loadLanguageConfig(onlinePlayer);
            onlinePlayer.sendMessage(player.getName() + langHandler.getMessage("leave.left"));
        }

        return true;
    }
}
