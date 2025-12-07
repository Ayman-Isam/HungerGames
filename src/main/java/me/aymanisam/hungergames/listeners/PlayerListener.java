package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
import me.aymanisam.hungergames.stats.DatabaseHandler;
import me.aymanisam.hungergames.stats.PlayerStatsHandler;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static me.aymanisam.hungergames.HungerGames.*;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.*;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teamsAlive;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.PROJECTILE;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.WORLD_BORDER;

public class PlayerListener implements Listener {
    private final HungerGames plugin;
    private final SetSpawnHandler setSpawnHandler;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;
    private final SignHandler signHandler;
    private final SignClickListener signClickListener;
	private final DatabaseHandler databaseHandler;
    private final ResetPlayerHandler resetPlayerHandler;

	private final Map<String, Map<Player, Location>> deathLocations = new HashMap<>();
    private final Map<Player, Set<Player>> playerDamagers = new HashMap<>();
    public static final Map<String, Map<Player, Integer>> playerKills = new HashMap<>();

    public PlayerListener(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, ScoreBoardHandler scoreBoardHandler) {
        this.setSpawnHandler = setSpawnHandler;
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.configHandler = plugin.getConfigHandler();
	    ArenaHandler arenaHandler = new ArenaHandler(plugin, langHandler);
        this.signHandler = new SignHandler(plugin, setSpawnHandler);
        this.signClickListener = new SignClickListener(plugin, langHandler, setSpawnHandler, arenaHandler, scoreBoardHandler);
	    this.databaseHandler = new DatabaseHandler(plugin);
        this.resetPlayerHandler = new ResetPlayerHandler();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        resetPlayerHandler.resetPlayer(player, player.getWorld());

        List<Player> worldPlayersWaiting = setSpawnHandler.playersWaiting.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());
        List<Player> worldPlayersPlacement = playerPlacements.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());

        if (gameStarted.getOrDefault(player.getWorld().getName(), false) || gameStarting.getOrDefault(player.getWorld().getName(), false)) {
            worldPlayersAlive.remove(player);
            if (configHandler.getWorldConfig(player.getWorld()).getInt("players-per-team") == 1) {
                worldPlayersPlacement.add(player);
            }
        } else {
            setSpawnHandler.removePlayerFromSpawnPoint(player, player.getWorld());
            worldPlayersWaiting.remove(player);
        }

        if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
		        try {
			        PlayerStatsHandler playerStats;
			        if (statsMap.containsKey(player.getUniqueId())) {
				        playerStats = statsMap.get(player.getUniqueId());
			        } else {
				        playerStats = databaseHandler.getPlayerStatsFromDatabase(player);
				        statsMap.put(player.getUniqueId(), playerStats);
			        }

			        playerStats.setLastLogout(new Date());

			        if (totalTimeSpent.containsKey(player)) {
				        int timeAlive = 0;
				        if (!player.getWorld().getName().equals(configHandler.getPluginSettings().getString("lobby-world"))) {
					        timeAlive = configHandler.getWorldConfig(player.getWorld()).getInt("game-time") - timeLeft.get(player.getWorld().getName());
				        }
				        Long timeSpent = totalTimeSpent.getOrDefault(player, 0L);
				        playerStats.setSecondsPlayed(playerStats.getSecondsPlayed() + timeAlive + timeSpent);
				        playerStats.setSecondsPlayedMonth(playerStats.getSecondsPlayedMonth() + timeAlive + timeSpent);
				        totalTimeSpent.remove(player);
			        }

			        playerStats.setDirty();

			        plugin.getDatabase().updatePlayerStats(playerStats);
		        } catch (SQLException e) {
			        plugin.getLogger().log(Level.SEVERE, e.toString());
		        }
	        });
        }

        removeFromTeam(player);

        signHandler.setSignContent();
    }

    private void removeFromTeam(Player player) {
        List<List<Player>> worldTeams = teams.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());
        List<List<Player>> worldTeamsAlive = teamsAlive.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());
        List<List<Player>> worldTeamPlacements = teamPlacements.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());

        for (List<Player> aliveTeam : worldTeamsAlive) {
            if (aliveTeam.contains(player)) {
                aliveTeam.remove(player);
                if (aliveTeam.isEmpty()) {
                    worldTeamsAlive.remove(aliveTeam);
                    for (List<Player> team: worldTeams) {
                        if (team.contains(player)) {
                            worldTeamPlacements.add(team);
                        }
                    }
                }
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        List<Player> worldPlayersWaiting = setSpawnHandler.playersWaiting.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());

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
        String lobbyWorldName = (String) configHandler.getPluginSettings().get("lobby-world");
        assert lobbyWorldName != null;
        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
        if (lobbyWorld != null) {
            player.teleport(lobbyWorld.getSpawnLocation());
        } else {
            plugin.getLogger().log(Level.SEVERE, "Could not find lobbyWorld [ " + lobbyWorldName + "]");
        }
        event.setJoinMessage(null);

        if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				try {
					PlayerStatsHandler playerStats;
					if (statsMap.containsKey(player.getUniqueId())) {
						playerStats = statsMap.get(player.getUniqueId());
					} else {
						playerStats = databaseHandler.getPlayerStatsFromDatabase(player);
						statsMap.put(player.getUniqueId(), playerStats);
					}

					playerStats.setUsername(player.getName());

					playerStats.setLastLogin(new Date());

					playerStats.setDirty();
				} catch (SQLException e) {
					plugin.getLogger().log(Level.SEVERE, e.toString());
				}
			});
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();

        List<Player> worldPlayersWaiting = setSpawnHandler.playersWaiting.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        List<Player> worldPlayersPlacement = playerPlacements.computeIfAbsent(player.getWorld().getName(), k -> new ArrayList<>());

        if (gameStarted.getOrDefault(world.getName(), false) || gameStarting.getOrDefault(world.getName(), false)) {
            worldPlayersAlive.remove(player);
            if (configHandler.getWorldConfig(world).getInt("players-per-team") == 1) {
                worldPlayersPlacement.add(player);
            }
            event.setDeathMessage(null);

            player.sendMessage(langHandler.getMessage(player, "game.placed", worldPlayersAlive.size() + 1));

            if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	            PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	            playerStats.setDeaths(playerStats.getDeaths() + 1);

	            EntityDamageEvent.DamageCause lastDamageCause = Objects.requireNonNull(player.getLastDamageCause()).getCause();

	            if (player.getKiller() != null) {
	                playerStats.setPlayerDeaths(playerStats.getPlayerDeaths() + 1);
	            } else if (lastDamageCause == WORLD_BORDER) {
	                playerStats.setBorderDeaths(playerStats.getBorderDeaths() + 1);
	            } else {
	                playerStats.setEnvironmentDeaths(playerStats.getEnvironmentDeaths() + 1);
	            }

	            playerStats.setDirty();
            }

        } else {
            setSpawnHandler.removePlayerFromSpawnPoint(player, world);
            worldPlayersWaiting.remove(player);
        }

        removeFromTeam(player);
        removeBossBar(player);

        signHandler.setSignContent();

        if (configHandler.getPluginSettings().getBoolean("spectating")) {
            if (gameStarted.getOrDefault(world.getName(), false)) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendTitle("", langHandler.getMessage(player, "spectate.spectating-player"), 5, 20, 10);
                player.sendMessage(langHandler.getMessage(player, "spectate.message"));
                Map<Player, Location> worldDeathLocations = deathLocations.computeIfAbsent(world.getName(), k -> new HashMap<>());
                worldDeathLocations.put(player, player.getLocation());
            }
        }

        Player killer = event.getEntity().getKiller();

        for (Player damager: playerDamagers.computeIfAbsent(player, k -> new HashSet<>())) {
            if (damager != killer) {
                if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	                PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());
	                playerStats.setKillAssists(playerStats.getKillAssists() + 1);

	                playerStats.setDirty();
                }
            }
        }

        if (killer != null) {
            Map<Player, Integer> worldPlayerKills = playerKills.computeIfAbsent(world.getName(), k -> new HashMap<>());
            worldPlayerKills.put(killer, worldPlayerKills.getOrDefault(killer, 0) + 1);
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

            if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	            PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	            playerStats.setKills(playerStats.getKills() + 1);

	            playerStats.setDirty();
            }
        }

        Location location = player.getLocation();
        world.spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 10);
        world.spawnParticle(Particle.REDSTONE, location, 50, new Particle.DustOptions(Color.RED, 10f));
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.4f, 1.0f);

        if (gameStarted.getOrDefault(world.getName(), false)) {
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
        Map<Player, Location> worldDeathLocations = deathLocations.computeIfAbsent(player.getWorld().getName(), k -> new HashMap<>());

        if (worldDeathLocations.containsKey(player)) {
            event.setRespawnLocation(worldDeathLocations.get(player));
            worldDeathLocations.remove(player);
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

        if (damager instanceof Player && damaged instanceof LivingEntity) {
            if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	            PlayerStatsHandler playerStats = statsMap.get(damager.getUniqueId());

	            playerStats.setDamageDealt(playerStats.getDamageDealt() + event.getDamage());

	            if (event.getCause() == PROJECTILE) {
	                playerStats.setProjectileDamageDealt(playerStats.getProjectileDamageDealt() + event.getDamage());
	            }

	            playerStats.setDirty();
            }
        }

        if (!(damaged instanceof Player player)) {
            return;
        }

        if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	        PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	        playerStats.setDamageTaken(playerStats.getDamageTaken() + event.getDamage());

	        if (event.getCause() == PROJECTILE) {
	            playerStats.setProjectileDamageTaken(playerStats.getProjectileDamageTaken() + event.getDamage());
	        }

	        playerStats.setDirty();
        }

        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

        if ((itemInMainHand.getType() == Material.SHIELD || itemInOffHand.getType() == Material.SHIELD) && player.isBlocking()) {
            if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	            PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());
	            playerStats.setAttacksBlocked(playerStats.getAttacksBlocked() + 1);

	            playerStats.setDirty();
            }
        }

        if (damager instanceof Player damagerPlayer) {
            List<List<Player>> worldTeams = teams.computeIfAbsent(damager.getWorld().getName(), k -> new ArrayList<>());

            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == PROJECTILE) {
                for (List<Player> team : worldTeams) {
                    if (team.contains(damagerPlayer) && team.contains(player)) {
                        event.setCancelled(true);
                        break;
                    }
                }
                playerDamagers.computeIfAbsent(player, k -> new HashSet<>()).add(damagerPlayer);
            }
        }
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (block != null && (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.BARREL)) {
                if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	                PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	                playerStats.setChestsOpened(playerStats.getChestsOpened() + 1);

	                playerStats.setDirty();
                }
            } else if (block != null && (block.getType() == Material.RED_SHULKER_BOX)) {
                if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	                PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	                playerStats.setSupplyDropsOpened(playerStats.getSupplyDropsOpened() + 1);

	                playerStats.setDirty();
                }
            }
        }
    }

    @EventHandler
    public void onProjectileShot(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();

        if (!(projectile instanceof Arrow|| projectile instanceof Firework)) {
            return;
        }

        if (!(projectile.getShooter() instanceof Player player)) {
            return;
        }


        if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	        PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	        if (projectile instanceof Arrow) {
	            playerStats.setArrowsShot(playerStats.getArrowsShot() + 1);
	        } else {
	            playerStats.setFireworksShot(playerStats.getFireworksShot() + 1);
	        }

	        playerStats.setDirty();
        }
    }

    @EventHandler
    public void onProjectileLanded(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (!(projectile instanceof Arrow || projectile instanceof SpectralArrow || projectile instanceof Firework)) {
            return;
        }

        if (!(projectile.getShooter() instanceof Player player)) {
            return;
        }

        if(!(event.getHitEntity() instanceof LivingEntity)) {
            return;
        }

        if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	        PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	        if (projectile instanceof Arrow || projectile instanceof SpectralArrow) {
	            playerStats.setArrowsLanded(playerStats.getArrowsLanded() + 1);
	        } else {
	            playerStats.setFireworksLanded(playerStats.getFireworksLanded() + 1);
	        }

	        playerStats.setDirty();
        }
    }

    @EventHandler
    public void onEntityRegenerateHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        double healthRegenerated = event.getAmount();

        if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	        PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	        playerStats.setHealthRegenerated(playerStats.getHealthRegenerated() + healthRegenerated);

	        playerStats.setDirty();
        }
    }

    @EventHandler
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack consumedItem = event.getItem();

        if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	        PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	        if (consumedItem.getType() == Material.POTION) {
	            playerStats.setPotionsUsed(playerStats.getPotionsUsed() + 1);
	        } else {
	            playerStats.setFoodConsumed(playerStats.getFoodConsumed() + 1);
	        }

	        playerStats.setDirty();
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!(event.getPotion().getShooter() instanceof Player player)) {
            return;
        }

        if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	        PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	        playerStats.setPotionsUsed(playerStats.getPotionsUsed() + 1);

	        playerStats.setDirty();
        }
    }

    @EventHandler
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	        PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	        playerStats.setPotionsUsed(playerStats.getPotionsUsed() + 1);

	        playerStats.setDirty();
        }
    }

    @EventHandler
    public void onTotemPopped(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }


        if (configHandler.getPluginSettings().getBoolean("database.enabled")) {
	        PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

	        playerStats.setTotemsPopped(playerStats.getTotemsPopped() + 1);

	        playerStats.setDirty();
        }
    }
}
