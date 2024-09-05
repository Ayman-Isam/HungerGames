package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.*;
import static me.aymanisam.hungergames.listeners.TeamVotingListener.giveVotingBook;

public class MapChangeCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ArenaHandler arenaHandler;
    private final ConfigHandler configHandler;

    public MapChangeCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!player.hasPermission("hungergames.map")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (gameStarted.getOrDefault(player.getWorld(), false) || gameStarting.getOrDefault(player.getWorld(), false)) {
            player.sendMessage(langHandler.getMessage(player, "map.game-running"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(langHandler.getMessage(player, "map.no-args"));
            return false;
        }

        String mapName = args[0];

        if (!worldNames.contains(mapName)) {
            sender.sendMessage(langHandler.getMessage(player, "map.not-found", mapName));
            plugin.getLogger().info("Loaded maps:" + plugin.getServer().getWorlds().stream().map(World::getName).collect(Collectors.joining(", ")));
            return false;
        }

        World world = Bukkit.getServer().createWorld(new WorldCreator(mapName));

        String worldName = world.getName();

        File worldFolder = new File(plugin.getDataFolder(), worldName);
        if (!worldFolder.exists()) {
            worldFolder.mkdirs();
        }
        arenaHandler.createArenaConfig(world);
        configHandler.createWorldConfig(world);
        configHandler.loadItemsConfig(world);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.teleport(world.getSpawnLocation());
            setSpawnHandler.teleportPlayerToSpawnpoint(p, world);
        }

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(player.getWorld(), k -> new HashMap<>());

        for (Player p : player.getWorld().getPlayers()) {
            if (worldSpawnPointMap.containsValue(p)) {
                setSpawnHandler.removePlayerFromSpawnPoint(p, world);
            }
        }

        sender.sendMessage(langHandler.getMessage(player, "map.switched", mapName));

        giveVotingBook(player, langHandler);

        return true;
    }
}
