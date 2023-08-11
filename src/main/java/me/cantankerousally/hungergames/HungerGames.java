package me.cantankerousally.hungergames;

import me.cantankerousally.hungergames.commands.*;
import me.cantankerousally.hungergames.handler.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


public final class HungerGames extends JavaPlugin {

    public HungerGames() {
    }
    public boolean gameStarted = false;
    public BossBar bossBar;
    private GameHandler gameHandler;

    public List<Player> playersAlive;


    @Override
    public void onEnable() {
        bossBar = getServer().createBossBar("Time Remaining", BarColor.BLUE, BarStyle.SOLID);
        SetSpawnHandler setSpawnHandler = new SetSpawnHandler(this);
        gameHandler = new GameHandler(this, setSpawnHandler);
        getServer().getWorld("world");
        playersAlive = new ArrayList<>();
        new CompassHandler(this);

        saveDefaultConfig();
        Objects.requireNonNull(getCommand("supplydrop")).setExecutor(new SupplyDropCommand(this));
        Objects.requireNonNull(getCommand("create")).setExecutor(new ArenaSelectorCommand(this));
        Objects.requireNonNull(getCommand("select")).setExecutor(new ArenaSelectorCommand(this));
        Objects.requireNonNull(getCommand("setspawn")).setExecutor(new SetSpawnCommand());
        Objects.requireNonNull(getCommand("chestrefill")).setExecutor(new ChestRefillCommand(this));
        Objects.requireNonNull(getCommand("start")).setExecutor(new StartGameCommand(this));
        Objects.requireNonNull(getCommand("end")).setExecutor(new EndGameCommand(this));
        getServer().getPluginManager().registerEvents(new SetArenaHandler(this), this);
        getServer().getPluginManager().registerEvents(setSpawnHandler, this);
        getServer().getPluginManager().registerEvents(new WorldBorderHandler(this), this);
        getServer().getPluginManager().registerEvents(gameHandler, this);
    }
    public GameHandler getGameHandler() {
        return gameHandler;
    }
}
