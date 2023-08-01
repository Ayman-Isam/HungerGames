package me.cantankerousally.hungergames;

import me.cantankerousally.hungergames.commands.*;
import me.cantankerousally.hungergames.handler.*;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public final class HungerGames extends JavaPlugin {
    public boolean gameStarted = false;
    public BossBar bossBar;
    private GameHandler gameHandler;
    private CompassHandler compassHandler;
    @Override
    public void onEnable() {
        bossBar = getServer().createBossBar("Time Remaining", BarColor.BLUE, BarStyle.SOLID);
        gameHandler = new GameHandler(this);
        World world = getServer().getWorld("world");

        saveDefaultConfig();
        getCommand("supplydrop").setExecutor(new SupplyDropCommand(this));
        getCommand("create").setExecutor(new ArenaSelectorCommand(this));
        getCommand("select").setExecutor(new ArenaSelectorCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("chestrefill").setExecutor(new ChestRefillCommand(this));
        getCommand("start").setExecutor(new StartGameCommand(this));
        getCommand("end").setExecutor(new EndGameCommand(this));
        getServer().getPluginManager().registerEvents(new SetArenaHandler(this), this);
        getServer().getPluginManager().registerEvents(new SetSpawnHandler(this), this);
        getServer().getPluginManager().registerEvents(new WorldBorderHandler(this), this);
        getServer().getPluginManager().registerEvents(gameHandler, this);
        compassHandler = new CompassHandler(this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    if (player.getName().startsWith(".")) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 0, true, false));
                    }
                }
            }
        }, 0L, 20L);
    }
    public GameHandler getGameHandler() {
        return gameHandler;
    }
}
