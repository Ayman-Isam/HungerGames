package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

import static me.aymanisam.hungergames.HungerGames.*;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class SignClickListener implements Listener {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ArenaHandler arenaHandler;
    private final ConfigHandler configHandler;
    private final Map<Player, Long> lastInteractTime = new HashMap<>();
    private final Map<Player, Long> lastMessageTime = new HashMap<>();

    public SignClickListener(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, ArenaHandler arenaHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.arenaHandler = arenaHandler;
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();

            Block block = event.getClickedBlock();
            assert block != null;

            long currentTime = System.currentTimeMillis();

            if (block.getState() instanceof Sign sign) {
                for (String worldName : worldNames) {
                    if (sign.getLine(1).contains(worldName)) {
                        if (lastInteractTime.containsKey(player) && (currentTime - lastInteractTime.get(player)) < 5000) {
                            return; // Ignore the event if it's within the cooldown period
                        }

                        World world = Bukkit.getWorld(worldName);

                        if (lastMessageTime.containsKey(player) && (currentTime - lastMessageTime.get(player)) < 200) {
                            return; // Don't send another message if within cooldown
                        }

                        if (gameStarting.getOrDefault(world, false)) {
                            player.sendMessage(langHandler.getMessage(player, "startgame.starting"));
                            lastMessageTime.put(player, currentTime);
                            return;
                        }

                        if (gameStarted.getOrDefault(world, false)) {
                            player.sendMessage(langHandler.getMessage(player, "startgame.started"));
                            lastMessageTime.put(player, currentTime);
                            return;
                        }

                        if (world == null) {
                            World createdWorld = Bukkit.createWorld(WorldCreator.name(worldName));
                            assert createdWorld != null;
                            arenaHandler.loadWorldFiles(createdWorld);
                            if (setSpawnHandler.playersWaiting.get(createdWorld) != null && setSpawnHandler.playersWaiting.get(createdWorld).contains(player)) {
                                return;
                            }
                            setSpawnHandler.teleportPlayerToSpawnpoint(player, createdWorld);
                            setSpawnHandler.createSetSpawnConfig(createdWorld);
                        } else {
                            setSpawnHandler.teleportPlayerToSpawnpoint(player, world);
                            setSpawnHandler.createSetSpawnConfig(world);
                        }

                        lastInteractTime.put(player, currentTime);
                        break;
                    }
                }
            }
        }
    }

    public void setSignContent(List<Location> locations) {
        List<String> worlds = new ArrayList<>(worldNames);
        Collections.sort(worlds);

        for (Location location : locations) {
            String worldName = worlds.get(0);
            World world = Bukkit.getWorld(worldName);

            int worldPlayersWaitingSize = setSpawnHandler.playersWaiting.computeIfAbsent(world, k -> new ArrayList<>()).size();
            int worldSpawnPointSize = setSpawnHandler.spawnPoints.computeIfAbsent(world, k -> new ArrayList<>()).size();
            List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());

            if (location.getBlock().getState() instanceof Sign sign) {
                sign.setEditable(false);
                sign.setLine(0, ChatColor.BOLD + "Join");
                sign.setLine(1, ChatColor.BOLD + worldName);
                if (isGameStartingOrStarted(world)) {
                    sign.setLine(2, ChatColor.BOLD + "In Progress");
                    sign.setLine(3, ChatColor.BOLD + "" + worldPlayersAlive.size() + " Alive");
                } else {
                    sign.setLine(2, ChatColor.BOLD + "Waiting");
                    sign.setLine(3, ChatColor.BOLD + "[" + worldPlayersWaitingSize + "/" + worldSpawnPointSize + "]");
                }
                sign.update();
            }

            worlds.remove(0);
        }
    }
}
