package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.listeners.TeamVotingListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static me.aymanisam.hungergames.HungerGames.gameStarted;
import static me.aymanisam.hungergames.HungerGames.gameStarting;

public class SetSpawnHandler {
    private final HungerGames plugin;
    private final ResetPlayerHandler resetPlayerHandler;
    private final LangHandler langHandler;
    private final TeamVotingListener teamVotingListener;
    private final ConfigHandler configHandler;

    public FileConfiguration setSpawnConfig;
    public Map<World, List<String>> spawnPoints;
    public Map<World, Map<String, Player>> spawnPointMap;
    public Map<World, List<Player>> playersWaiting;
    private File setSpawnFile;

    public SetSpawnHandler(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.spawnPoints = new HashMap<>();
        this.spawnPointMap = new HashMap<>();
        this.playersWaiting = new HashMap<>();
        this.resetPlayerHandler = new ResetPlayerHandler();
        this.teamVotingListener = new TeamVotingListener(plugin, langHandler);
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    public void createSetSpawnConfig(World world) {
        File worldFolder = new File(plugin.getDataFolder() + File.separator + world.getName());
        setSpawnFile = new File(worldFolder, "setspawn.yml");
        if (!setSpawnFile.exists()) {
            setSpawnFile.getParentFile().mkdirs();
            plugin.saveResource("setspawn.yml", false);
        }

        setSpawnConfig = YamlConfiguration.loadConfiguration(setSpawnFile);
        List<String> worldSpawnPoints = setSpawnConfig.getStringList("spawnpoints");
        spawnPoints.put(world, worldSpawnPoints);
    }

    public void saveSetSpawnConfig(World world) {
        if (setSpawnConfig == null || setSpawnFile == null) {
            return;
        }
        try {
            List<String> worldSpawnPoints = spawnPoints.computeIfAbsent(world, k -> new ArrayList<>());

            setSpawnConfig.set("spawnpoints", worldSpawnPoints);

            setSpawnConfig.save(setSpawnFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not save config to the specified location." + setSpawnFile, ex);
        }
    }

    public void resetSpawnPoints(World world) {
        List<String> worldSpawnPoints = spawnPoints.computeIfAbsent(world, k -> new ArrayList<>());

        worldSpawnPoints.clear();
        setSpawnConfig.set("spawnpoints", worldSpawnPoints);
        saveSetSpawnConfig(world);
    }

    public String assignPlayerToSpawnPoint(Player player, World world) {
        createSetSpawnConfig(world);

        List<String> worldSpawnPoints = spawnPoints.computeIfAbsent(world, k -> new ArrayList<>());

        List<String> shuffledSpawnPoints = new ArrayList<>(worldSpawnPoints);
        Collections.shuffle(shuffledSpawnPoints);

        Map<String, Player> worldSpawnPointMap = spawnPointMap.computeIfAbsent(world, k-> new HashMap<>());

        for (String spawnPoint : shuffledSpawnPoints) {
            if (!worldSpawnPointMap.containsKey(spawnPoint)) {
                return spawnPoint;
            }
        }

        player.sendMessage(langHandler.getMessage(player, "game.join-fail"));
        return null;
    }

    public void removePlayerFromSpawnPoint(Player player, World world) {
        Map<String, Player> worldSpawnPointMap = spawnPointMap.computeIfAbsent(world, k-> new HashMap<>());

        Iterator<Map.Entry<String, Player>> iterator = worldSpawnPointMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Player> entry = iterator.next();
            if (entry.getValue().equals(player)) {
                iterator.remove();
                break;
            }
        }
    }

    public void teleportPlayerToSpawnpoint(Player player, World world) {
        String spawnPoint = assignPlayerToSpawnPoint(player, world);

        if (spawnPoint == null) {
            return;
        }

        Map<String, Player> worldSpawnPointMap = spawnPointMap.computeIfAbsent(world, k-> new HashMap<>());
        List<Player> worldPlayersWaiting = playersWaiting.computeIfAbsent(world, k -> new ArrayList<>());
        List<String> worldSpawnPoints = spawnPoints.computeIfAbsent(world, k -> new ArrayList<>());

        worldSpawnPointMap.put(spawnPoint, player);
        worldPlayersWaiting.add(player);

        String[] coords = spawnPoint.split(",");
        double x = Double.parseDouble(coords[1]) + 0.5;
        double y = Double.parseDouble(coords[2]) + 1.0;
        double z = Double.parseDouble(coords[3]) + 0.5;

        Location teleportLocation = new Location(world, x, y, z);

        assert world != null;
        Location spawnLocation = world.getSpawnLocation();

        Vector direction = spawnLocation.toVector().subtract(teleportLocation.toVector());

        float yaw = (float) (Math.toDegrees(Math.atan2(direction.getZ(), direction.getX())) - 90);

        teleportLocation.setYaw(yaw);

        player.teleport(teleportLocation);

        for (Player onlinePlayer : world.getPlayers()) {
            onlinePlayer.sendMessage(langHandler.getMessage(onlinePlayer, "setspawn.joined-message", player.getName(), worldSpawnPointMap.size(), worldSpawnPoints.size()));
        }

        resetPlayerHandler.resetPlayer(player);

        if (configHandler.getWorldConfig(world).getBoolean("voting")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (worldSpawnPointMap.containsValue(player) && !gameStarted.getOrDefault(player.getWorld(), false) && !gameStarting.getOrDefault(player.getWorld(), false)) {
                    teamVotingListener.openVotingInventory(player);
                }
            }, 100L);
        }
    }
}
