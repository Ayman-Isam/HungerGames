package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.*;
import static me.aymanisam.hungergames.handlers.CountDownHandler.playersPerTeam;
import static me.aymanisam.hungergames.handlers.SetSpawnHandler.spawnPointMap;

public class StartGameCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ArenaHandler arenaHandler;
    private final CountDownHandler countDownHandler;
    private final ConfigHandler configHandler;
    private final DatabaseHandler databaseHandler;

    public StartGameCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, CountDownHandler countDownHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
        this.countDownHandler = countDownHandler;
        this.configHandler = plugin.getConfigHandler();
        this.databaseHandler = new DatabaseHandler(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player player = null;

        if (sender instanceof Player) {
            player = ((Player) sender);
        }

        if (player != null && !player.hasPermission("hungergames.start")) {
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

        if (gameStarted.getOrDefault(world.getName(), false)) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.started"));
            return true;
        }

        if (gameStarting.getOrDefault(world.getName(), false)) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.starting"));
            return true;
        }

        String worldName = arenaHandler.getArenaConfig(world).getString("region.world");

        if (worldName == null) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.set-arena"));
            return true;
        }

        List<String> worldSpawnPoints = setSpawnHandler.spawnPoints.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        if (worldSpawnPoints.isEmpty()) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.set-spawn"));
            return true;
        }

        int minPlayers = configHandler.getWorldConfig(world).getInt("min-players");

        Map<String, Player> worldSpawnPointMap = spawnPointMap.computeIfAbsent(world.getName(), k -> new HashMap<>());

        if (worldSpawnPointMap.size() < minPlayers) {
            sender.sendMessage(langHandler.getMessage(player, "startgame.min-players", minPlayers));
            return true;
        }

        gameStarting.put(world.getName(), true);

        countDownHandler.startCountDown(world);

        if (player != null && configHandler.getPluginSettings().getBoolean("database.enabled")) {
	        PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	        if (playersPerTeam != 1) {
	            playerStats.setTeamGamesStarted(playerStats.getTeamGamesStarted() + 1);
	        } else {
	            playerStats.setSoloGamesStarted(playerStats.getSoloGamesStarted() + 1);
	        }

	        playerStats.setDirty();
        }

        return true;
    }
}
