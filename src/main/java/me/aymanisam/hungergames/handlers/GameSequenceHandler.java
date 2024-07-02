package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Level;

import static me.aymanisam.hungergames.HungerGames.gameWorld;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teamsAlive;
import static me.aymanisam.hungergames.listeners.PlayerListener.playerKills;
import static me.aymanisam.hungergames.listeners.TeamVotingListener.playerVotes;

public class GameSequenceHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ArenaHandler arenaHandler;
    private final TeamsHandler teamsHandler;
    private final WorldBorderHandler worldBorderHandler;
    private final ScoreBoardHandler scoreBoardHandler;
    private final ResetPlayerHandler resetPlayerHandler;
    private final ConfigHandler configHandler;
    private final WorldResetHandler worldResetHandler;

    public int gracePeriodTaskId;
    public int timerTaskId;
    public static int timeLeft;
    public BukkitTask chestRefillTask;
    public BukkitTask supplyDropTask;
    public static final List<Player> playersAlive = new ArrayList<>();
    public static Map<Player, BossBar> playerBossBars = new HashMap<>();

    public GameSequenceHandler(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.setSpawnHandler = setSpawnHandler;
        this.arenaHandler = new ArenaHandler(plugin);
        this.teamsHandler = new TeamsHandler(plugin);
        this.worldBorderHandler = new WorldBorderHandler(plugin);
        this.scoreBoardHandler = new ScoreBoardHandler(plugin);
        this.resetPlayerHandler = new ResetPlayerHandler();
        this.configHandler = new ConfigHandler(plugin);
        this.worldResetHandler = new WorldResetHandler(plugin);
    }

    public void startGame() {
        HungerGames.gameStarted = true;

        setSpawnHandler.playersWaiting.clear();
        setSpawnHandler.spawnPointMap.clear();

        worldBorderHandler.startWorldBorder(gameWorld);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            langHandler.getLangConfig(player);
            player.sendTitle("", langHandler.getMessage("game.start"),5,20,10);
            player.sendMessage(langHandler.getMessage("game.grace-start"));
        }

        int gracePeriod = configHandler.getWorldConfig(gameWorld).getInt("grace-period");
        gracePeriodTaskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            World world = gameWorld;
            assert world != null;
            world.setPVP(true);
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                langHandler.getLangConfig(player);
                player.sendMessage(langHandler.getMessage("game.grace-end"));
                player.sendTitle("", langHandler.getMessage("game.grace-end"),5,20,10);
                player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);
            }
        }, gracePeriod * 20L);

        for (Player player : playersAlive) {
            langHandler.getLangConfig(player);
            BossBar bossBar = plugin.getServer().createBossBar(langHandler.getMessage("time-remaining"), BarColor.GREEN, BarStyle.SOLID);
            bossBar.addPlayer(player);

            playerBossBars.put(player, bossBar);

            if (configHandler.getWorldConfig(gameWorld).getBoolean("bedrock-buff.enabled") && player.getName().startsWith(".")) {
                List<String> effectNames = configHandler.getWorldConfig(gameWorld).getStringList("bedrock-buff.effects");
                for (String effectName : effectNames) {
                    PotionEffectType effectType = PotionEffectType.getByName(effectName);
                    if (effectType != null) {
                        player.addPotionEffect(new PotionEffect(effectType, 200000, 1, true, false));
                    }
                }
            }
        }

        int supplyDropInterval = configHandler.getWorldConfig(gameWorld).getInt("supplydrop.interval") * 20;
        SupplyDropHandler supplyDropHandler = new SupplyDropHandler(plugin);

        supplyDropTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            supplyDropHandler.setSupplyDrop(gameWorld);
        }, supplyDropInterval, supplyDropInterval);

        int chestRefillInterval = configHandler.getWorldConfig(gameWorld).getInt("chestrefill.interval") * 20;
        ChestRefillHandler chestRefillHandler = new ChestRefillHandler(plugin);

        chestRefillTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            chestRefillHandler.refillChests(gameWorld);
        }, 0, chestRefillInterval);

        mainGame();
    }

    public void mainGame() {
        timeLeft = configHandler.getWorldConfig(gameWorld).getInt("game-time");

        timerTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<Player, BossBar> entry : playerBossBars.entrySet()) {
                BossBar bossBar = entry.getValue();
                bossBar.setProgress((double) timeLeft / configHandler.getWorldConfig(gameWorld).getInt("game-time"));
                int minutes = (timeLeft - 1) / 60;
                int seconds = (timeLeft - 1) % 60;
                String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                bossBar.setTitle(langHandler.getMessage("score.time", timeFormatted));
            }
            timeLeft--;

            scoreBoardHandler.getScoreBoard();

            if (configHandler.getWorldConfig(gameWorld).getInt("players-per-team") > 1) {
                if (teamsAlive.size() <= 1) {
                    plugin.getServer().getScheduler().cancelTask(timerTaskId);

                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        langHandler.getLangConfig(player);
                        player.sendMessage(langHandler.getMessage("game.game-end"));
                    }

                    List<Player> winningTeamAlive = teamsAlive.get(0);
                    List<Player> winningTeam = null;

                    for (List<Player> team: teams) {
                        for (Player player : winningTeamAlive) {
                            if (team.contains(player)) {
                                winningTeam = team;
                                break;
                            }
                        }
                        if (winningTeam != null) {
                            break;
                        }
                    }

                    winningTeam(winningTeam, null);

                    endGame();
                }
            } else {
                if (playersAlive.size() <= 1) {
                    plugin.getServer().getScheduler().cancelTask(timerTaskId);

                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        langHandler.getLangConfig(player);
                        player.sendMessage(langHandler.getMessage("game.game-end"));
                    }

                    Player winner = playersAlive.get(0);
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        langHandler.getLangConfig(player);
                        player.sendMessage(langHandler.getMessage("game.winner-text", winner.getName()));
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    }
                    endGame();
                }
            }

            if (timeLeft < 0) {
                if (configHandler.getWorldConfig(gameWorld).getInt("players-per-team") > 1) {
                    determineWinningTeam();
                    endGame();
                } else {
                    Player winner = null;
                    int maxKills = -1;
                    for (Map.Entry<Player, Integer> entry : playerKills.entrySet()) {
                        if (entry.getValue() > maxKills) {
                            maxKills = entry.getValue();
                            winner = entry.getKey();
                        }
                        if (winner != null) {
                            for (Player player : plugin.getServer().getOnlinePlayers()) {
                                langHandler.getLangConfig(player);
                                player.sendTitle("", langHandler.getMessage("game.winner-kills", winner.getName()), 5, 20, 10);
                                player.sendMessage(langHandler.getMessage("game.winner-kills", winner.getName()));
                                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                            }
                        }
                    }
                    endGame();
                }
            }
        }, 0L, 20L);
    }

    private void winningTeam(List<Player> winningTeam, String winReason) {
        if (winningTeam != null) {
            StringBuilder allNames = new StringBuilder();
            for (Player teamMember : winningTeam) {
                allNames.append(teamMember.getName()).append(", ");
            }

            if (!allNames.isEmpty()) {
                allNames.setLength(allNames.length() - 2);
            }

            String messageKey;
            if ("kills".equals(winReason)) {
                messageKey = "game.timeup-kills";
            } else if ("alive".equals(winReason)) {
                messageKey = "game.timeup-alive";
            } else {
                messageKey = "game.winner";
            }

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                langHandler.getLangConfig(player);
                player.sendTitle("", langHandler.getMessage(messageKey, allNames.toString()), 5, 20, 10);
                player.sendMessage(langHandler.getMessage(messageKey, allNames.toString()));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
        }
    }

    private void determineWinningTeam() {
        List<Player> winningTeam = null;
        int maxAlivePlayers = -1;
        int maxKills = -1;
        String winReason = null;
        for (List<Player> team : teamsAlive) {
            int alivePlayers = team.size();
            int teamKills = 0;
            for (Player player : team) {
                teamKills += playerKills.getOrDefault(player, 0);
            }
            if (alivePlayers > maxAlivePlayers) {
                maxAlivePlayers = alivePlayers;
                maxKills = teamKills;
                winningTeam = team;
                winReason = "alive";
            } else if (alivePlayers == maxAlivePlayers && teamKills > maxKills) {
                maxKills = teamKills;
                winningTeam = team;
                winReason = "kills";
            } else if (alivePlayers == maxAlivePlayers) {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    langHandler.getLangConfig(player);
                    player.sendMessage(langHandler.getMessage("game.timeup-no-winner"));
                }
            }
        }

        winningTeam(winningTeam, winReason);
    }

    public void endGame() {
        World world = gameWorld;

        HungerGames.gameStarted = false;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            resetPlayerHandler.resetPlayer(player);
            removeBossBar(player);
            player.teleport(player.getWorld().getSpawnLocation());
            scoreBoardHandler.removeScoreboard(player);
            playerBossBars.remove(player);
        }

        worldBorderHandler.resetWorldBorder(world);

        worldResetHandler.removeShulkers(world);

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

        plugin.getServer().getScheduler().cancelTask(timerTaskId);
        plugin.getServer().getScheduler().cancelTask(gracePeriodTaskId);

        if (chestRefillTask != null) {
            plugin.getServer().getScheduler().cancelTask(chestRefillTask.getTaskId());
        }

        if (supplyDropTask != null) {
            plugin.getServer().getScheduler().cancelTask(supplyDropTask.getTaskId());
        }

        playersAlive.clear();

        playerVotes.clear();

        if (plugin.isEnabled()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    player.sendMessage(langHandler.getMessage("game.join-instruction"));
                    player.sendTitle("",langHandler.getMessage("game.join-instruction"), 5, 20, 10);
                }
            }, 100L);

            if (configHandler.getWorldConfig(gameWorld).getBoolean("auto-join")) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        if (!setSpawnHandler.spawnPointMap.containsValue(player)) {
                            player.sendMessage(langHandler.getMessage("game.auto-join"));
                            player.sendTitle("", langHandler.getMessage("game.auto-join"), 5, 20, 10);
                            setSpawnHandler.teleportPlayerToSpawnpoint(player);
                        }
                    }
                }, 200L);
            }
        }
    }

    public void removeBossBar(Player player) {
        BossBar bossBar = playerBossBars.get(player);
        if (bossBar != null) {
            bossBar.removePlayer(player);
            playerBossBars.remove(player);
        }
    }
}
