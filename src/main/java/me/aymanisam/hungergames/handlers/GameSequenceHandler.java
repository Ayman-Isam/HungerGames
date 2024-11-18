package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.listeners.CompassListener;
import me.aymanisam.hungergames.listeners.SignClickListener;
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
import org.bukkit.scoreboard.Team;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.HungerGames.*;
import static me.aymanisam.hungergames.handlers.CountDownHandler.playersPerTeam;
import static me.aymanisam.hungergames.handlers.ScoreBoardHandler.startingPlayers;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;
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
    private final SignHandler signHandler;
    private final SignClickListener signClickListener;
    private final DatabaseHandler databaseHandler;

    public Map<World, Integer> gracePeriodTaskId = new HashMap<>();
    public Map<World, Integer> timerTaskId = new HashMap<>();
    public static Map<World, Integer> timeLeft = new HashMap<>();
    public Map<World, BukkitTask> chestRefillTask = new HashMap<>();
    public Map<World, BukkitTask> supplyDropTask = new HashMap<>();
    public static Map<World, List<Player>> playersAlive = new HashMap<>();
    public static Map<World, Map<Player, BossBar>> playerBossBars = new HashMap<>();
    public static Map<World, List<Player>> playerPlacements = new HashMap<>();
    public static Map<World, List<List<Player>>> teamPlacements = new HashMap<>();

    public GameSequenceHandler(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, CompassListener compassListener, TeamsHandler teamsHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.worldBorderHandler = new WorldBorderHandler(plugin, langHandler);
        this.scoreBoardHandler = new ScoreBoardHandler(plugin, langHandler);
        this.resetPlayerHandler = new ResetPlayerHandler();
        this.configHandler = plugin.getConfigHandler();
        this.worldResetHandler = new WorldResetHandler(plugin, langHandler);
        this.compassListener = compassListener;
        this.teamsHandler = teamsHandler;
        this.signHandler = new SignHandler(plugin);
        this.signClickListener = new SignClickListener(plugin, langHandler, setSpawnHandler, new ArenaHandler(plugin, langHandler));
        this.databaseHandler = new DatabaseHandler(plugin);
    }

    public void startGame(World world) {
        gameStarted.put(world, true);
        gameStarting.put(world, false);

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(world, k -> new HashMap<>());
        List<Player> worldPlayersWaiting = setSpawnHandler.playersWaiting.computeIfAbsent(world, k -> new ArrayList<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());

        worldPlayersWaiting.clear();
        startingPlayers.put(world, worldSpawnPointMap.values().size());
        worldSpawnPointMap.clear();

        signClickListener.setSignContent(signHandler.loadSignLocations());

        worldBorderHandler.startWorldBorder(world);

        for (Player player : world.getPlayers()) {
            player.sendTitle("", langHandler.getMessage(player, "game.start"), 5, 20, 10);
            player.sendMessage(langHandler.getMessage(player, "game.grace-start"));

            Long timeSpent = totalTimeSpent.getOrDefault(player, 0L);
            totalTimeSpent.put(player, timeSpent);

            try {
                PlayerStatsHandler playerStats = databaseHandler.getPlayerStatsFromDatabase(player);

                if (configHandler.getWorldConfig(world).getInt("players-per-team") == 1) {
                    playerStats.setSoloGamesPlayed(playerStats.getSoloGamesPlayed() + 1);
                } else {
                    playerStats.setTeamGamesPlayed(playerStats.getTeamGamesPlayed() + 1);
                }

                this.plugin.getDatabase().updatePlayerStats(playerStats);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, e.toString());
            }
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
        chestRefillTask.put(world, worldChestRefillTask);

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

            if (currentTimeLeft <= 0) {
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
        List<List<Player>> worldTeamPlacements = teamPlacements.computeIfAbsent(world, k -> new ArrayList<>());

        if (worldTeamsAlive.size() == 1) {
            List<Player> winningTeam = worldTeamsAlive.get(0);
            worldTeamPlacements.add(winningTeam);
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
        List<Player> worldPlayerPlacements = playerPlacements.computeIfAbsent(world, k -> new ArrayList<>());

        Player winner = worldPlayersAlive.isEmpty() ? null : worldPlayersAlive.get(0);

        if (winner != null) {
            worldPlayerPlacements.add(winner);

            try {
                PlayerStatsHandler playerStats = databaseHandler.getPlayerStatsFromDatabase(winner);

                playerStats.setSoloGamesWon(playerStats.getSoloGamesWon() + 1);

                this.plugin.getDatabase().updatePlayerStats(playerStats);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, e.toString());
            }
        }

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

            for (Player player: winningTeam) {
                try {
                    PlayerStatsHandler playerStats = databaseHandler.getPlayerStatsFromDatabase(player);

                    playerStats.setTeamGamesWon(playerStats.getTeamGamesWon() + 1);

                    this.plugin.getDatabase().updatePlayerStats(playerStats);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, e.toString());
                }
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

        List<Player> worldPlayerPlacements = playerPlacements.get(world);
        List<List<Player>> worldTeamPlacements = teamPlacements.get(world);

        if (configHandler.getWorldConfig(world).getInt("players-per-team") == 1) {
            if (worldPlayerPlacements != null && worldPlayerPlacements.size() == startingPlayers.get(world)) {
                for (Player player : worldPlayerPlacements) {
                    int playerIndex = worldPlayerPlacements.indexOf(player);
                    double percentile = (1 - (playerIndex / (worldPlayerPlacements.size() - 1.0))) * 100.0;
                    System.out.println(percentile);

                    try {
                        PlayerStatsHandler playerStats = databaseHandler.getPlayerStatsFromDatabase(player);

                        double netPercentile = (playerStats.getSoloPercentile() * playerStats.getSoloGamesPlayed() + percentile) / (playerStats.getSoloGamesPlayed() + 1);
                        playerStats.setSoloPercentile(netPercentile);

                        this.plugin.getDatabase().updatePlayerStats(playerStats);
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, e.toString());
                    }
                }
            }
        } else {
            System.out.println("TeamPlacements: " + teamPlacements);
            System.out.println("Teams: " + teams);
            if (worldTeamPlacements != null && worldTeamPlacements.size()  == teams.get(world).size()) {
                System.out.println("YIPPEEEEEEEEEEE");
                for (List<Player> team : worldTeamPlacements) {
                    int teamIndex = worldTeamPlacements.indexOf(team);
                    double percentile = (1 - (teamIndex / (worldTeamPlacements.size() - 1.0))) * 100.0;
                    System.out.println(percentile);

                    for (Player player : team) {
                        try {
                            PlayerStatsHandler playerStats = databaseHandler.getPlayerStatsFromDatabase(player);

                            double netPercentile = (playerStats.getTeamPercentile() * playerStats.getTeamGamesPlayed() + percentile) / (playerStats.getTeamGamesPlayed() + 1);

                            playerStats.setTeamPercentile(netPercentile);
                            System.out.println("Setting team percentile for player " + player.getName() + " to " + netPercentile);

                            this.plugin.getDatabase().updatePlayerStats(playerStats);
                            System.out.println("Updated player stats for player " + player.getName());
                        } catch (SQLException e) {
                            plugin.getLogger().log(Level.SEVERE, e.toString());
                        }
                    }
                }
            }
        }

        playerPlacements.clear();
        teamPlacements.clear();

        for (Player player : world.getPlayers()) {
            resetPlayerHandler.resetPlayer(player);
            removeBossBar(player);
            String lobbyWorldName = (String) configHandler.createPluginSettings().get("lobby-world");
            assert lobbyWorldName != null;
            World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
            assert lobbyWorld != null;
            player.teleport(lobbyWorld.getSpawnLocation());
            scoreBoardHandler.removeScoreboard(player);

            if (!disable && totalTimeSpent.containsKey(player)) {
                Long timeSpent = totalTimeSpent.getOrDefault(player, 0L);
                int timeAlive = configHandler.getWorldConfig(world).getInt("game-time") - timeLeft.getOrDefault(world, 0);
                totalTimeSpent.put(player, timeSpent + timeAlive);
            }
        }

        worldBorderHandler.resetWorldBorder(world);

        worldResetHandler.removeShulkers(world);

        if (!disable && configHandler.createPluginSettings().getBoolean("reset-world")) {
            worldResetHandler.sendToWorld(world);
            worldResetHandler.resetWorldState(world);
        }

        world.getEntitiesByClass(Item.class).forEach(Item::remove);
        world.getEntitiesByClass(ExperienceOrb.class).forEach(ExperienceOrb::remove);
        world.getEntitiesByClass(Arrow.class).forEach(Arrow::remove);
        world.getEntitiesByClass(Trident.class).forEach(Trident::remove);

        world.setPVP(false);

        if (gracePeriodTaskId.containsKey(world)) {
            int worldGracePeriodTaskId = gracePeriodTaskId.get(world);
            plugin.getServer().getScheduler().cancelTask(worldGracePeriodTaskId);
        }

        if (timerTaskId.containsKey(world)) {
            int worldTimerTaskId = timerTaskId.get(world);
            plugin.getServer().getScheduler().cancelTask(worldTimerTaskId);
        }

        BukkitTask worldChestRefillTask = chestRefillTask.get(world);
        BukkitTask worldSupplyDropTask = supplyDropTask.get(world);

        if (worldChestRefillTask != null) {
            plugin.getServer().getScheduler().cancelTask(worldChestRefillTask.getTaskId());
        }

        if (worldSupplyDropTask != null) {
            plugin.getServer().getScheduler().cancelTask(worldSupplyDropTask.getTaskId());
        }

        compassListener.cancelGlowTask(world);
        teamsHandler.removeGlowFromAllPlayers(world);

        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world, k -> new ArrayList<>());
        Map<Player, String> worldPlayerVotes = playerVotes.computeIfAbsent(world, k -> new HashMap<>());

        worldPlayersAlive.clear();
        worldPlayerVotes.clear();

        signClickListener.setSignContent(signHandler.loadSignLocations());

        if (plugin.isEnabled()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                for (Player player : world.getPlayers()) {
                    player.sendMessage(langHandler.getMessage(player, "game.join-instruction"));
                    player.sendTitle("", langHandler.getMessage(player, "game.join-instruction"), 5, 20, 10);
                }
            }, 100L);
        }
    }

    public static void removeBossBar(Player player) {
        Map<Player, BossBar> worldPlayerBossBar = playerBossBars.computeIfAbsent(player.getWorld(), k -> new HashMap<>());

        BossBar bossBar = worldPlayerBossBar.get(player);
        if (bossBar != null) {
            bossBar.removePlayer(player);
            worldPlayerBossBar.remove(player);
            bossBar.setVisible(false);
        }
    }
}
