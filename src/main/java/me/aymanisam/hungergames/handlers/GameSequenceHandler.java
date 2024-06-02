package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.commands.ChestRefillCommand;
import me.aymanisam.hungergames.commands.SupplyDropCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teamsAlive;

public class GameSequenceHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ArenaHandler arenaHandler;
    private final TeamsHandler teamsHandler;
    private final WorldBorderHandler worldBorderHandler;
    private final ScoreBoardHandler scoreBoardHandler;
    private final ResetPlayerHandler resetPlayerHandler;

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
    }

    public void startGame() {
        HungerGames.gameStarted = true;

        setSpawnHandler.playersWaiting.clear();
        setSpawnHandler.spawnPointMap.clear();

        worldBorderHandler.startWorldBorder();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            langHandler.getLangConfig(player);
            player.sendTitle("", langHandler.getMessage("game.game-start"),5,20,10);
            player.sendMessage(langHandler.getMessage("game.grace-start"));
        }

        int gracePeriod = plugin.getConfig().getInt("grace-period");
        gracePeriodTaskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            World world = plugin.getServer().getWorld(Objects.requireNonNull(arenaHandler.getArenaConfig().getString("region.world")));
            assert world != null;
            world.setPVP(true);
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                langHandler.getLangConfig(player);
                player.sendMessage(langHandler.getMessage("game.grace-end"));
            }
        }, gracePeriod * 20L);

        for (Player player : playersAlive) {
            langHandler.getLangConfig(player);
            BossBar bossBar = plugin.getServer().createBossBar(langHandler.getMessage("time-remaining"), BarColor.GREEN, BarStyle.SOLID);
            bossBar.addPlayer(player);

            playerBossBars.put(player, bossBar);

            if (plugin.getConfig().getBoolean("bedrock-buff.enabled") && player.getName().startsWith(".")) {
                List<String> effectNames = plugin.getConfig().getStringList("bedrock-buff.effects");
                for (String effectName : effectNames) {
                    PotionEffectType effectType = PotionEffectType.getByName(effectName);
                    if (effectType != null) {
                        player.addPotionEffect(new PotionEffect(effectType, 200000, 1, true, false));
                    }
                }
            }
        }

        int supplyDropInterval = plugin.getConfig().getInt("supplydrop.interval") * 20;
        SupplyDropCommand supplyDropCommand = new SupplyDropCommand(plugin);
        PluginCommand supplyDropPluginCommand = plugin.getCommand("supplydrop");

        supplyDropTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            supplyDropCommand.onCommand(
                    plugin.getServer().getConsoleSender(), supplyDropPluginCommand, "supplydrop", new String[0]);
        }, supplyDropInterval, supplyDropInterval);

        int chestRefillInterval = plugin.getConfig().getInt("chestrefill.interval") * 20;
        ChestRefillCommand chestRefillCommand = new ChestRefillCommand(plugin);
        PluginCommand chestRefillPluginCommand = plugin.getCommand("chestrefill");

        chestRefillTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            chestRefillCommand.onCommand(
                    plugin.getServer().getConsoleSender(), chestRefillPluginCommand, "chestrefill", new String[0]);
        }, 0, chestRefillInterval);

        mainGame();
    }

    public void mainGame() {
        timeLeft = plugin.getConfig().getInt("game-time");

        timerTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<Player, BossBar> entry : playerBossBars.entrySet()) {
                BossBar bossBar = entry.getValue();
                bossBar.setProgress((double) timeLeft / plugin.getConfig().getInt("game-time"));
                int minutes = (timeLeft - 1) / 60;
                int seconds = (timeLeft - 1) % 60;
                String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                bossBar.setTitle(langHandler.getMessage("game.score-time") + timeFormatted);
            }
            timeLeft--;

            scoreBoardHandler.getScoreBoard();

            if (plugin.getConfig().getInt("players-per-team") > 1) {
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

                    if (winningTeam != null) {
                        StringBuilder allNames = new StringBuilder();
                        for (Player teamMember : winningTeam) {
                            allNames.append(teamMember.getName()).append(", ");
                        }

                        if (!allNames.isEmpty()) {
                            allNames.setLength(allNames.length() - 2);
                        }

                        for (Player player : plugin.getServer().getOnlinePlayers()) {
                            langHandler.getLangConfig(player);
                            player.sendTitle("", ChatColor.LIGHT_PURPLE + allNames.toString() + langHandler.getMessage("game.winner-text"), 5, 20, 10);
                            player.sendMessage(ChatColor.LIGHT_PURPLE + allNames.toString() + langHandler.getMessage("game.winner-text"));
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        }
                    }

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
                        player.sendMessage(ChatColor.LIGHT_PURPLE + winner.getName() + langHandler.getMessage("game.winner-text"));
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    }
                    endGame();
                }
            }

            if (timeLeft < 0) {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    langHandler.getLangConfig(player);
                    player.sendMessage(langHandler.getMessage("game.time-up"));
                }
                endGame();
            }
        }, 0L, 20L);
    }

    public void endGame() {
        HungerGames.gameStarted = false;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            resetPlayerHandler.resetPlayer(player);
            removeBossBar(player);
            player.teleport(player.getWorld().getSpawnLocation());
            scoreBoardHandler.removeScoreboard(player);
            playerBossBars.remove(player);
        }

        worldBorderHandler.resetWorldBorder();

        String worldName = arenaHandler.getArenaConfig().getString("region.world");
        assert worldName != null;
        World world = plugin.getServer().getWorld(worldName);

        assert world != null;
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

        arenaHandler.removeShulkers();

        playersAlive.clear();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                player.sendMessage(langHandler.getMessage("game.join-instruction"));
                player.sendTitle("",langHandler.getMessage("game.join-instruction"), 5, 20, 10);
            }
        }, 100L);
    }

    public void removeBossBar(Player player) {
        BossBar bossBar = playerBossBars.get(player);
        if (bossBar != null) {
            bossBar.removePlayer(player);
            playerBossBars.remove(player);
        }
    }
}
