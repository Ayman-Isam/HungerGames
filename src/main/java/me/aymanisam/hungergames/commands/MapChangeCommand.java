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
import java.util.logging.Level;
import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.isGameStartingOrStarted;
import static me.aymanisam.hungergames.HungerGames.worldNames;

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
        this.configHandler = plugin.getConfigHandler();
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

        if (isGameStartingOrStarted(player.getWorld().getName())) {
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

        File worldFolder = new File(plugin.getDataFolder(), mapName);
        if (!worldFolder.exists()) {
            try {
                if (!worldFolder.mkdirs()) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to create directories for " + mapName);
                }
            } catch (SecurityException e) {
                plugin.getLogger().log(Level.SEVERE, "No permission to create folders", e);
            }
        }

        if (world == null) {
            plugin.getLogger().log(Level.SEVERE, "Could not switch to the map, world is null");
            player.sendMessage(langHandler.getMessage(player, "border.wrong-world"));
            return true;
        }

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(player.getWorld().getName(), k -> new HashMap<>());

        for (Player p : player.getWorld().getPlayers()) {
            if (worldSpawnPointMap.containsValue(p)) {
                setSpawnHandler.removePlayerFromSpawnPoint(p, world);
            }
        }

        arenaHandler.createArenaConfig(world);
        configHandler.createWorldConfig(world);
        configHandler.loadItemsConfig(world);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.teleport(world.getSpawnLocation());
            setSpawnHandler.teleportPlayerToSpawnpoint(p, world);
        }

        sender.sendMessage(langHandler.getMessage(player, "map.switched", mapName));

        return true;
    }
}
