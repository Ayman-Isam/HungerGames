package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.HungerGames.gameStarted;
import static me.aymanisam.hungergames.HungerGames.gameWorld;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teamsAlive;

public class PlayerListener implements Listener {
    private final HungerGames plugin;
    private final SetSpawnHandler setSpawnHandler;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;

    private final Map<Player, Location> deathLocations = new HashMap<>();
    public static final Map<Player, Integer> playerKills = new HashMap<>();

    public PlayerListener(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.setSpawnHandler = setSpawnHandler;
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.configHandler = new ConfigHandler(plugin);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        if (gameStarted) {
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
        player.teleport(gameWorld.getSpawnLocation());

        boolean spectating = configHandler.getWorldConfig(player.getWorld()).getBoolean("spectating");
        if (spectating) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        boolean autoJoin = configHandler.getWorldConfig(player.getWorld()).getBoolean("auto-join");
        if (autoJoin && !gameStarted && !HungerGames.gameStarting) {
            setSpawnHandler.teleportPlayerToSpawnpoint(player);
            event.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (gameStarted) {
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

        System.out.println(teamsAlive);

        World world = plugin.getServer().getWorld("world");
        assert world != null;

        boolean spectating = configHandler.getWorldConfig(gameWorld).getBoolean("spectating");
        if (spectating) {
            player.setGameMode(GameMode.SPECTATOR);
            if (gameStarted) {
                player.sendTitle("", langHandler.getMessage("spectate.spectating-player"), 5, 20, 10);
                player.sendMessage(langHandler.getMessage("spectate.message"));
                deathLocations.put(player, player.getLocation());
            }
        }

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            playerKills.put(killer, playerKills.getOrDefault(killer, 0) + 1);
            List<Map<?, ?>> effectMaps = configHandler.getWorldConfig(gameWorld).getMapList("killer-effects");
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

        if (gameStarted) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                langHandler.getLangConfig(p);
                if (killer != null)
                    p.sendMessage(langHandler.getMessage("game.killed-message", player.getName(), killer.getName()));
                else {
                    p.sendMessage(langHandler.getMessage("game.death-message", player.getName()));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (deathLocations.containsKey(player)) {
            event.setRespawnLocation(deathLocations.get(player));
            deathLocations.remove(player);
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

        if (event.getEntityType() == EntityType.PLAYER) {
            if (event.getDamager().getType().equals(EntityType.ENDER_PEARL)) {
                event.setDamage(2.0);
            }
        }
    }
}
