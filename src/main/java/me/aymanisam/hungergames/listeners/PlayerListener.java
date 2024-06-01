package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playerBossBars;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teamsAlive;

public class PlayerListener implements Listener {
    private final HungerGames plugin;
    private final SetSpawnHandler setSpawnHandler;
    private final LangHandler langHandler;

    public PlayerListener(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.setSpawnHandler = setSpawnHandler;
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (HungerGames.gameStarted) {
            playersAlive.remove(player);
        } else {
            setSpawnHandler.removePlayerFromSpawnPoint(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (setSpawnHandler.playersWaiting.contains(player.getName())) {
            Location from = event.getFrom();
            Location to = event.getTo();

            assert to != null;
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.teleport(player.getWorld().getSpawnLocation());

        boolean spectating = plugin.getConfig().getBoolean("spectating");
        if (spectating) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        boolean autoJoin = plugin.getConfig().getBoolean("auto-join");
        if (autoJoin && !HungerGames.gameStarted) {
            setSpawnHandler.teleportPlayerToSpawnpoint(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (HungerGames.gameStarted) {
            playersAlive.remove(player);
            event.setDeathMessage(null);
        } else if (HungerGames.gameStarting){
            playersAlive.remove(player);
        } else {
            setSpawnHandler.removePlayerFromSpawnPoint(player);
        }

        for (List<Player> team: teamsAlive) {
            if (team.contains(player)) {
                team.remove(player);
                if (team.isEmpty()) {
                    teamsAlive.remove(team);
                }
                break;
            }
        }

        World world = plugin.getServer().getWorld("world");
        assert world != null;
        Location spawnLocation = world.getSpawnLocation();

        boolean spectating = plugin.getConfig().getBoolean("spectating");
        if (spectating) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendTitle("", langHandler.getMessage("spectate.spectating-player"), 5, 20, 10);
            player.sendMessage(langHandler.getMessage("spectate.message"));
            player.teleport(player.getLocation());
        } else {
            player.teleport(spawnLocation);
        }

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            List<Map<?, ?>> effectMaps = plugin.getConfig().getMapList("killer-effects");
            for (Map<?, ?> effectMap : effectMaps) {
                String effectName = (String) effectMap.get("effect");
                int duration = (int) effectMap.get("duration");
                int level = (int) effectMap.get("level");
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    killer.addPotionEffect(new PotionEffect(effectType, duration, level));
                }
            }
        }

        Location location = player.getLocation();
        world.spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 10);
        world.spawnParticle(Particle.REDSTONE, location, 50, new Particle.DustOptions(Color.RED, 10f));
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.4f, 1.0f);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            langHandler.getLangConfig(p);
            if (killer != null)
                p.sendMessage(player.getName() + langHandler.getMessage("game.killed-message") + killer.getName());
            else {
                p.sendMessage(player.getName() + langHandler.getMessage("game.death-message"));
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            if (event.getClickedBlock() != null) {
                Material blockType = event.getClickedBlock().getType();
                if (blockType == Material.CHEST || blockType == Material.TRAPPED_CHEST || blockType == Material.BARREL || blockType == Material.RED_SHULKER_BOX) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (plugin.getConfig().getInt("players-per-team") < 2) {
            return;
        }

        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        if (damager instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player) {
                damager = (Player) arrow.getShooter();
            }
        } else if (damager instanceof Trident trident) {
            if (trident.getShooter() instanceof Player) {
                damager = (Player) trident.getShooter();
            }
        } else if (damager instanceof SpectralArrow spectralArrow) {
            if (spectralArrow.getShooter() instanceof Player) {
                damager = (Player) spectralArrow.getShooter();
            }
        }

        if (damager instanceof Player damagerPlayer && damaged instanceof Player damagedPlayer) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                for (List<Player> team : teams) {
                    if (team.contains(damagerPlayer) && team.contains(damagedPlayer)) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }
}
