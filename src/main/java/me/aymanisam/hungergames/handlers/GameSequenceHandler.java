package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.listeners.CompassListener;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.HungerGames.gameStarted;
import static me.aymanisam.hungergames.handlers.CountDownHandler.playersPerTeam;
import static me.aymanisam.hungergames.handlers.ScoreBoardHandler.startingPlayers;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teamsAlive;
import static me.aymanisam.hungergames.listeners.PlayerListener.playerKills;
import static me.aymanisam.hungergames.listeners.TeamVotingListener.playerVotes;

public class GameSequenceHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final WorldBorderHandler worldBorderHandler;
    private final ScoreBoardHandler scoreBoardHandler;
    private final ResetPlayerHandler resetPlayerHandler;
    private final ConfigHandler configHandler;
    private final WorldResetHandler worldResetHandler;
    private final CompassListener compassListener;
    private final TeamsHandler teamsHandler;

    public Map<World, Integer> gracePeriodTaskId = new HashMap<>();
    public Map<World, Integer> timerTaskId = new HashMap<>();
    public static Map<World, Integer> timeLeft = new HashMap<>();
    public Map<World, BukkitTask> chestRefillTask = new HashMap<>();
    public Map<World, BukkitTask> supplyDropTask = new HashMap<>();
    public static Map<World, List<Player>> playersAlive = new HashMap<>();
    public static Map<World, Map<Player, BossBar>> playerBossBars = new HashMap<>();

    public GameSequenceHandler(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, CompassListener compassListener, TeamsHandler teamsHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.worldBorderHandler = new WorldBorderHandler(plugin, langHandler);
        this.scoreBoardHandler = new ScoreBoardHandler(plugin, langHandler);
        this.resetPlayerHandler = new ResetPlayerHandler();
        this.configHandler = new ConfigHandler(plugin, langHandler);
        this.worldResetHandler = new WorldResetHandler(plugin, langHandler);
        this.compassListener = compassListener;
        this.teamsHandler = teamsHandler;
    }

    public void startGame(World world) {
        gameStarted.put(world, true);

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(world, k -> new HashMap<>());
        List<Player> worldPlayersWaiting = setSpawnHandler.playersWaiting.computeIfAbsent(world, k -> new ArrayList<>());

        worldPlayersWaiting.clear();
        startingPlayers.put(world, worldSpawnPointMap.values().size());
        worldSpawnPointMap.clear();

        worldBorderHandler.startWorldBorder(world);

        for (Player player : world.getPlayers()) {
            player.sendTitle("", langHandler.getMessage(player, "game.start"), 5, 20, 10);
            player.sendMessage(langHandler.getMessage(player, "game.grace-start"));
        }

        int gracePeriod = configHandler.getWorldConfig(world).getInt("grace-period");
        int worldGracePeriodTaskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            world.setPVP(true);
            for (Player player : world.getPlayers()) {
                player.sendMessage(langHandler.getMessage(player, "game.grace-end"));
                player.sendTitle("", langHandler.getMessage(player, "game.grace-end"), 5, 20, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);
            }
        }, gracePeriod * 20L);

        gracePeriodTaskId.put(world, worldGracePeriodTaskId);

        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());

        for (Player player : worldPlayersAlive) {
            BossBar bossBar = plugin.getServer().createBossBar(langHandler.getMessage(player, "time-remaining"), BarColor.GREEN, BarStyle.SOLID);
            bossBar.addPlayer(player);

            Map<Player, BossBar> worldPlayerBossBars = playerBossBars.computeIfAbsent(world, k -> new HashMap<>());

            worldPlayerBossBars.put(player, bossBar);

            if (configHandler.getWorldConfig(world).getBoolean("bedrock-buff.enabled") && player.getName().startsWith(".")) {
                List<String> effectNames = configHandler.getWorldConfig(world).getStringList("bedrock-buff.effects");
                for (String effectName : effectNames) {
                    PotionEffectType effectType = PotionEffectType.getByName(effectName);
                    if (effectType != null) {
                        player.addPotionEffect(new PotionEffect(effectType, 200000, 1, true, false));
                    }
                }
            }
        }

        int supplyDropInterval = configHandler.getWorldConfig(world).getInt("supplydrop.interval") * 20;
        SupplyDropHandler supplyDropHandler = new SupplyDropHandler(plugin, langHandler);

        BukkitTask worldSupplyDropTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> supplyDropHandler.setSupplyDrop(world), supplyDropInterval, supplyDropInterval);
        supplyDropTask.put(world, worldSupplyDropTask);

        int chestRefillInterval = configHandler.getWorldConfig(world).getInt("chestrefill.interval") * 20;
        ChestRefillHandler chestRefillHandler = new ChestRefillHandler(plugin, langHandler);

        BukkitTask worldChestRefillTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> chestRefillHandler.refillChests(world), 0, chestRefillInterval);
        supplyDropTask.put(world, worldChestRefillTask);

        mainGame(world);
    }

    public void mainGame(World world) {
        int initialTimeLeft = configHandler.getWorldConfig(world).getInt("game-time");
        timeLeft.put(world, initialTimeLeft);

        int worldTimerTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            updateBossBars(world);
            int currentTimeLeft = timeLeft.get(world);
            currentTimeLeft--;
            timeLeft.put(world, currentTimeLeft);

            scoreBoardHandler.getScoreBoard(world);

            List<List<Player>> worldTeamsAlive = teamsAlive.computeIfAbsent(world, k -> new ArrayList<>());
            List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());

            if (playersPerTeam != 1) {
                if (worldTeamsAlive.size() <= 1) {
                    endGameWithTeams(world);
                }
            } else {
                if (worldPlayersAlive.size() <= 1) {
                    endGameWithPlayers(world);
                }
            }

            if (currentTimeLeft < 0) {
                handleTimeUp(world);
            }
        }, 0L, 20L);
        timerTaskId.put(world, worldTimerTaskId);
    }

    private void updateBossBars(World world) {
        Map<Player, BossBar> worldPlayerBossBars = playerBossBars.computeIfAbsent(world, k -> new HashMap<>());

        int worldTimeLeft = timeLeft.get(world);

        for (Map.Entry<Player, BossBar> entry : worldPlayerBossBars.entrySet()) {
            Player player = entry.getKey();
            BossBar bossBar = entry.getValue();
            bossBar.setProgress((double) worldTimeLeft / configHandler.getWorldConfig(world).getInt("game-time"));
            int minutes = (worldTimeLeft - 1) / 60;
            int seconds = (worldTimeLeft - 1) % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            bossBar.setTitle(langHandler.getMessage(player, "score.time", timeFormatted));
        }
    }

    private void endGameWithTeams(World world) {
        for (Player player : world.getPlayers()) {
            player.sendMessage(langHandler.getMessage(player, "game.game-end"));
        }

        List<List<Player>> worldTeamsAlive = teamsAlive.computeIfAbsent(world, k -> new ArrayList<>());

        if (worldTeamsAlive.size() == 1) {
            List<Player> winningTeam = worldTeamsAlive.get(0);
            winningTeam(winningTeam, "winner", world);
        } else {
            determineWinningTeam(world);
        }

        endGame(false, world);
    }

    private void endGameWithPlayers(World world) {
        for (Player player : world.getPlayers()) {
            player.sendMessage(langHandler.getMessage(player, "game.game-end"));
        }

        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());

        Player winner = worldPlayersAlive.isEmpty() ? null : worldPlayersAlive.get(0);
        for (Player player : world.getPlayers()) {
            if (winner != null) {
                player.sendMessage(langHandler.getMessage(player, "game.winner", winner.getName()));
                player.sendTitle("", langHandler.getMessage(player, "game.winner", winner.getName()), 5, 20, 10);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            } else {
                player.sendMessage(langHandler.getMessage(player, "game.team-no-winner"));
            }
        }
        endGame(false, world);
    }

    private void handleTimeUp(World world) {
        if (playersPerTeam != 1) {
            determineWinningTeam(world);
            endGame(false, world);
        } else {
            determineSoloWinner(world);
        }
    }

    private void determineSoloWinner(World world) {
        Player winner = null;
        int maxKills = -1;
        for (Map.Entry<Player, Integer> entry : playerKills.entrySet()) {
            if (entry.getValue() > maxKills) {
                maxKills = entry.getValue();
                winner = entry.getKey();
            }
        }

        for (Player player : world.getPlayers()) {
            if (winner != null) {
                player.sendTitle("", langHandler.getMessage(player, "game.solo-kills", winner.getName()), 5, 20, 10);
                player.sendMessage(langHandler.getMessage(player, "game.solo-kills", winner.getName()));
            } else {
                player.sendTitle("", langHandler.getMessage(player, "game.team-no-winner"), 5, 20, 10);
                player.sendMessage(langHandler.getMessage(player, "game.team-no-winner"));
            }

            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        endGame(false, world);
    }

    private void winningTeam(List<Player> winningTeam, String winReason, World world) {
        if (winningTeam != null) {
            String allNames = getAllPlayerNames(winningTeam);
            String messageKey = getMessageKey(winReason);
            String titleKey = getTitleKey(winReason);

            for (Player player : world.getPlayers()) {
                player.sendTitle("", langHandler.getMessage(player, messageKey, allNames), 5, 20, 10);
                player.sendMessage(langHandler.getMessage(player, titleKey, allNames));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
        }
    }

    private String getAllPlayerNames(List<Player> players) {
        StringBuilder allNames = new StringBuilder();
        for (Player player : players) {
            allNames.append(player.getName()).append(", ");
        }
        if (!allNames.isEmpty()) {
            allNames.setLength(allNames.length() - 2); // Remove trailing comma and space
        }
        return allNames.toString();
    }

    private String getMessageKey(String winReason) {
        return switch (winReason) {
            case "winner" -> "game.winner";
            case "team-kills" -> "game.team-kills";
            case "team-alive" -> "game.team-alive";
            default -> "game.team-no-winner";
        };
    }

    private String getTitleKey(String winReason) {
        return switch (winReason) {
            case "winner" -> "game.winner";
            case "team-kills", "team-alive" -> "game.time-up";
            default -> "game.team-no-winner";
        };
    }

    private void determineWinningTeam(World world) {
        List<List<Player>> potentialWinningTeams = new ArrayList<>();
        int maxAlivePlayers = -1;
        int maxKills = -1;

        List<List<Player>> worldTeamsAlive = teamsAlive.computeIfAbsent(world, k -> new ArrayList<>());

        for (List<Player> team : worldTeamsAlive) {
            int alivePlayers = team.size();
            int teamKills = team.stream().mapToInt(player -> playerKills.getOrDefault(player, 0)).sum();

            if (alivePlayers > maxAlivePlayers || (alivePlayers == maxAlivePlayers && teamKills > maxKills)) {
                maxAlivePlayers = alivePlayers;
                maxKills = teamKills;
                potentialWinningTeams.clear();
                potentialWinningTeams.add(team);
            } else if (alivePlayers == maxAlivePlayers && teamKills == maxKills) {
                potentialWinningTeams.add(team);
            }
        }

        if (potentialWinningTeams.size() == 1) {
            List<Player> winningTeam = potentialWinningTeams.get(0);
            String winReason = maxAlivePlayers > 0 ? "team-alive" : "team-kills";
            winningTeam(winningTeam, winReason, world);
        } else {
            for (Player player : world.getPlayers()) {
                player.sendMessage(langHandler.getMessage(player, "game.team-no-winner"));
            }
        }
    }

    public void endGame(Boolean disable, World world) {
        gameStarted.put(world, false);

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(world, k -> new HashMap<>());

        for (Player player : world.getPlayers()) {
            resetPlayerHandler.resetPlayer(player);
            removeBossBar(player);
            player.teleport(player.getWorld().getSpawnLocation());
            scoreBoardHandler.removeScoreboard(player);
            Map<Player, BossBar> worldPlayerBossBar = playerBossBars.computeIfAbsent(world, k -> new HashMap<>());
            worldPlayerBossBar.remove(player);
        }

        worldBorderHandler.resetWorldBorder(world);

        worldResetHandler.removeShulkers(world);

        if (!disable && configHandler.getWorldConfig(world).getBoolean("reset-world")) {
            worldResetHandler.sendToWorld(world);
            worldResetHandler.resetWorldState(world);
        }

        if (!world.getEntitiesByClass(Item.class).isEmpty()) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "kill @e[type=item]");
        }

        if (!world.getEntitiesByClass(ExperienceOrb.class).isEmpty()) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "kill @e[type=experience_orb]");
        }

        if (!world.getEntitiesByClass(Arrow.class).isEmpty()) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "kill @e[type=arrow]");
        }

        if (!world.getEntitiesByClass(Trident.class).isEmpty()) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "kill @e[type=trident]");
        }

        world.setPVP(false);

        int worldGracePeriodTaskId = gracePeriodTaskId.get(world);
        int worldTimerTaskId = timerTaskId.get(world);
        BukkitTask worldChestRefillTask = chestRefillTask.get(world);
        BukkitTask worldSupplyDropTask = supplyDropTask.get(world);

        plugin.getServer().getScheduler().cancelTask(worldTimerTaskId);
        plugin.getServer().getScheduler().cancelTask(worldGracePeriodTaskId);

        if (worldChestRefillTask != null) {
            plugin.getServer().getScheduler().cancelTask(worldChestRefillTask.getTaskId());
        }

        if (worldSupplyDropTask != null) {
            plugin.getServer().getScheduler().cancelTask(worldSupplyDropTask.getTaskId());
        }

        compassListener.cancelGlowTask(world);
        teamsHandler.removeGlowFromAllPlayers(world);

        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());

        worldPlayersAlive.clear();

        playerVotes.clear();

        if (plugin.isEnabled()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                for (Player player : world.getPlayers()) {
                    player.sendMessage(langHandler.getMessage(player, "game.join-instruction"));
                    player.sendTitle("", langHandler.getMessage(player, "game.join-instruction"), 5, 20, 10);
                }
            }, 100L);

            if (configHandler.getWorldConfig(world).getBoolean("auto-join")) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    for (Player player : world.getPlayers()) {
                        if (!worldSpawnPointMap.containsValue(player)) {
                            player.sendMessage(langHandler.getMessage(player, "game.auto-join"));
                            player.sendTitle("", langHandler.getMessage(player, "game.auto-join"), 5, 20, 10);
                            setSpawnHandler.teleportPlayerToSpawnpoint(player, world);
                        }
                    }
                }, 200L);
            }
        }
    }

    public void removeBossBar(Player player) {
        Map<Player, BossBar> worldPlayerBossBar = playerBossBars.computeIfAbsent(player.getWorld(), k -> new HashMap<>());

        BossBar bossBar = worldPlayerBossBar.get(player);
        if (bossBar != null) {
            bossBar.removePlayer(player);
            worldPlayerBossBar.remove(player);
        }
    }
}
