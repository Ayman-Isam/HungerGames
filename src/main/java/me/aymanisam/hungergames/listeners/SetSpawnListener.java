package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class SetSpawnListener implements Listener {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;

    public SetSpawnListener(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.setSpawnHandler = setSpawnHandler;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        langHandler.loadLanguageConfig(player);
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.STICK && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(langHandler.getMessage("setspawn.stick-name"))) {
            if (!(player.hasPermission("hungergames.setspawn"))) {
                player.sendMessage(langHandler.getMessage("no-permission"));
                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location location = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                FileConfiguration config = plugin.getConfig();
                String newSpawnPoint = Objects.requireNonNull(location.getWorld()).getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();

                if (setSpawnHandler.spawnPoints.contains(newSpawnPoint)) {
                    player.sendMessage(langHandler.getMessage("setspawnhandler.duplicate"));
                    event.setCancelled(true);
                    return;
                }

                if (setSpawnHandler.spawnPoints.size() >= config.getInt("max-players")) {
                    player.sendMessage(langHandler.getMessage("setspawnhandler.max-spawn"));
                    event.setCancelled(true);
                    return;
                }

                setSpawnHandler.spawnPoints.add(newSpawnPoint);
                setSpawnHandler.saveSetSpawnConfig();
                player.sendMessage(langHandler.getMessage("setspawnhandler.position-set") +
                        setSpawnHandler.spawnPoints.size() + langHandler.getMessage("setspawnhandler.set-at") + location.getBlockX() +
                        langHandler.getMessage("setspawnhandler.coord-y") + location.getBlockY() + langHandler.getMessage("setspawnhandler.coord-z") + location.getBlockZ());
            }
            event.setCancelled(true);
        }
    }
}
