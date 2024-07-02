package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ConfigHandler;
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
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ConfigHandler configHandler;

    public SetSpawnListener(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.langHandler = new LangHandler(plugin);
        this.setSpawnHandler = setSpawnHandler;
        this.configHandler = new ConfigHandler(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        langHandler.getLangConfig(player);
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.STICK && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(langHandler.getMessage("setspawn.stick-name"))) {
            if (!(player.hasPermission("hungergames.setspawn"))) {
                player.sendMessage(langHandler.getMessage("no-permission"));
                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location location = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                FileConfiguration config = configHandler.getWorldConfig(player.getWorld());
                String newSpawnPoint = Objects.requireNonNull(location.getWorld()).getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();

                if (setSpawnHandler.spawnPoints.contains(newSpawnPoint)) {
                    player.sendMessage(langHandler.getMessage("setspawn.duplicate"));
                    event.setCancelled(true);
                    return;
                }

                if (setSpawnHandler.spawnPoints.size() >= config.getInt("max-players")) {
                    player.sendMessage(langHandler.getMessage("setspawn.max-spawn"));
                    event.setCancelled(true);
                    return;
                }

                setSpawnHandler.spawnPoints.add(newSpawnPoint);
                setSpawnHandler.saveSetSpawnConfig();
                player.sendMessage(langHandler.getMessage("setspawn.position", setSpawnHandler.spawnPoints.size(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            }
            event.setCancelled(true);
        }
    }
}
