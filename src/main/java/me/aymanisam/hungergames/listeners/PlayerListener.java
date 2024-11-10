package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static me.aymanisam.hungergames.HungerGames.*;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.removeBossBar;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teamsAlive;

public class PlayerListener implements Listener {
    private final HungerGames plugin;
    private final SetSpawnHandler setSpawnHandler;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;
    private final ArenaHandler arenaHandler;
    private final SignHandler signHandler;
    private final SignClickListener signClickListener;
    private final ScoreBoardHandler scoreBoardHandler;

    private final Map<Player, Location> deathLocations = new HashMap<>();
    public static final Map<Player, Integer> playerKills = new HashMap<>();

    public PlayerListener(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, ScoreBoardHandler scoreBoardHandler) {
        this.setSpawnHandler = setSpawnHandler;
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.configHandler = plugin.getConfigHandler();
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
        this.signHandler = new SignHandler(plugin);
        this.signClickListener = new SignClickListener(plugin, langHandler, setSpawnHandler, arenaHandler);
        this.scoreBoardHandler = scoreBoardHandler;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        List<Player> worldPlayersWaiting = setSpawnHandler.playersWaiting.computeIfAbsent(player.getWorld(), k -> new ArrayList<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(player.getWorld(), k -> new ArrayList<>());

        if (gameStarted.getOrDefault(player.getWorld(), false) || gameStarting.getOrDefault(player.getWorld(), false)) {
            worldPlayersAlive.remove(player);
        } else {
            setSpawnHandler.removePlayerFromSpawnPoint(player, player.getWorld());
            worldPlayersWaiting.remove(player);
        }

        removeFromTeam(player);

        signClickListener.setSignContent(signHandler.loadSignLocations());
    }

    private void removeFromTeam(Player player) {
        List<List<Player>> worldTeamsAlive = teamsAlive.computeIfAbsent(player.getWorld(), k -> new ArrayList<>());

        for (List<Player> team : worldTeamsAlive) {
            if (team.contains(player)) {
                team.remove(player);
                if (team.isEmpty()) {
                    worldTeamsAlive.remove(team);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        List<Player> worldPlayersWaiting = setSpawnHandler.playersWaiting.computeIfAbsent(player.getWorld(), k -> new ArrayList<>());

        if (worldPlayersWaiting.contains(player)) {
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
        String lobbyWorldName = (String) configHandler.createPluginSettings().get("lobby-world");
        assert lobbyWorldName != null;
        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
        if (lobbyWorld != null) {
            player.teleport(lobbyWorld.getSpawnLocation());
        } else {
            plugin.getLogger().log(Level.SEVERE, "Could not find lobbyWorld [ " + lobbyWorldName + "]");
        }
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();

        List<Player> worldPlayersWaiting = setSpawnHandler.playersWaiting.computeIfAbsent(world, k -> new ArrayList<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());

        if (gameStarted.getOrDefault(world, false) || gameStarting.getOrDefault(world, false)) {
            worldPlayersAlive.remove(player);
            event.setDeathMessage(null);
        } else {
            setSpawnHandler.removePlayerFromSpawnPoint(player, world);
            worldPlayersWaiting.remove(player);
        }

        removeFromTeam(player);
        removeBossBar(player);

        scoreBoardHandler.removeScoreboard(player);

        signClickListener.setSignContent(signHandler.loadSignLocations());

        boolean spectating = configHandler.createPluginSettings().getBoolean("spectating");
        if (spectating) {
            if (gameStarted.getOrDefault(world, false)) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendTitle("", langHandler.getMessage(player, "spectate.spectating-player"), 5, 20, 10);
                player.sendMessage(langHandler.getMessage(player, "spectate.message"));
                deathLocations.put(player, player.getLocation());
            }
        }

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            playerKills.put(killer, playerKills.getOrDefault(killer, 0) + 1);
            List<Map<?, ?>> effectMaps = configHandler.getWorldConfig(world).getMapList("killer-effects");
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

        if (gameStarted.getOrDefault(world, false)) {
            for (Player p : world.getPlayers()) {
                langHandler.getLangConfig(p);
                if (killer != null)
                    p.sendMessage(langHandler.getMessage(player, "game.killed-message", player.getName(), killer.getName()));
                else {
                    p.sendMessage(langHandler.getMessage(player, "game.death-message", player.getName()));
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
        } else if (damager instanceof Firework firework) {
            if (firework.getShooter() instanceof Player) {
                damager = (Player) firework.getShooter();
            }
        }

        List<List<Player>> worldTeams = teams.computeIfAbsent(damager.getWorld(), k -> new ArrayList<>());

        if (damager instanceof Player damagerPlayer && damaged instanceof Player damagedPlayer) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                for (List<Player> team : worldTeams) {
                    if (team.contains(damagerPlayer) && team.contains(damagedPlayer)) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }
}
