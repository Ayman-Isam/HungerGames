package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.HungerGames.gameStarted;
import static me.aymanisam.hungergames.HungerGames.worldNames;

public class SignClickListener implements Listener {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;

    public SignClickListener(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();

            Block block = event.getClickedBlock();
            assert block != null;

            if (block.getState() instanceof Sign sign) {
                for (String worldName : worldNames) {
                    if (sign.getLine(1).contains(worldName)) {
                        World world = Bukkit.getWorld(worldName);
                        if (world == null) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                World createdWorld = Bukkit.createWorld(WorldCreator.name(worldName));
                                if (setSpawnHandler.playersWaiting.get(createdWorld).contains(player)) {
                                    return;
                                }
                                setSpawnHandler.teleportPlayerToSpawnpoint(player, createdWorld);
                            });
                        } else {
                            setSpawnHandler.teleportPlayerToSpawnpoint(player, world);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void setSignContent(List<Location> locations) {
        String lobbyWorld = plugin.getConfig().getString("lobby-world");
        List<String> worlds = new ArrayList<>(worldNames);
        worlds.remove(lobbyWorld);

        for (Location location : locations) {
            String worldName = worlds.get(0);

            if (location.getBlock().getState() instanceof Sign sign) {
                sign.setEditable(false);
                sign.setLine(0, "Join");
                sign.setLine(1, worldName);
                sign.setGlowingText(true);
                sign.update();
            }

            worlds.remove(0);
        }
    }
}
