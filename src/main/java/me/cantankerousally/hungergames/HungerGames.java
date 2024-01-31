package me.cantankerousally.hungergames;

import me.cantankerousally.hungergames.commands.*;
import me.cantankerousally.hungergames.handler.*;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public final class HungerGames extends JavaPlugin {

    public HungerGames() {
    }

    public boolean gameStarted = false;
    public BossBar bossBar;
    private GameHandler gameHandler;
    public List<Player> playersAlive;
    private SetSpawnHandler setSpawnHandler;
    private ChestRefillCommand chestRefillCommand;

    @Override
    public void onEnable() {
        bossBar = getServer().createBossBar("Time Remaining", BarColor.BLUE, BarStyle.SOLID);
        PlayerSignClickManager playerSignClickManager = new PlayerSignClickManager();
        setSpawnHandler = new SetSpawnHandler(this, playerSignClickManager);
        gameHandler = new GameHandler(this, setSpawnHandler, playerSignClickManager);
        getServer().getWorld("world");
        playersAlive = new ArrayList<>();
        new CompassHandler(this);
        chestRefillCommand = new ChestRefillCommand(this);

        World world = getServer().getWorld("world");
        if (world != null) {
            double borderSize = getConfig().getDouble("border.size");
            double centerX = getConfig().getDouble("border.center-x");
            double centerZ = getConfig().getDouble("border.center-z");
            WorldBorder border = world.getWorldBorder();
            border.setSize(borderSize);
            border.setCenter(centerX, centerZ);
        }

        saveDefaultConfig();
        saveResource("items.yml", false);
        Objects.requireNonNull(getCommand("supplydrop")).setExecutor(new SupplyDropCommand(this));
        Objects.requireNonNull(getCommand("create")).setExecutor(new ArenaSelectorCommand(this));
        Objects.requireNonNull(getCommand("select")).setExecutor(new ArenaSelectorCommand(this));
        Objects.requireNonNull(getCommand("setspawn")).setExecutor(new SetSpawnCommand(this));
        Objects.requireNonNull(getCommand("chestrefill")).setExecutor(new ChestRefillCommand(this));
        Objects.requireNonNull(getCommand("start")).setExecutor(new StartGameCommand(this));
        Objects.requireNonNull(getCommand("end")).setExecutor(new EndGameCommand(this));
        Objects.requireNonNull(getCommand("scanarena")).setExecutor(new ScanArenaCommand(this));
        MoveDisableHandler moveDisableHandler = new MoveDisableHandler(this, chestRefillCommand, playerSignClickManager);
        BorderSetCommand borderSetCommand = new BorderSetCommand(this);
        Objects.requireNonNull(getCommand("border")).setExecutor(borderSetCommand);
        Objects.requireNonNull(getCommand("border")).setTabCompleter(borderSetCommand);
        getServer().getPluginManager().registerEvents(new SetArenaHandler(this), this);
        getServer().getPluginManager().registerEvents(setSpawnHandler, this);
        getServer().getPluginManager().registerEvents(new WorldBorderHandler(this), this);
        getServer().getPluginManager().registerEvents(gameHandler, this);
        getServer().getPluginManager().registerEvents(moveDisableHandler, this);
    }

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public SetSpawnHandler getSetSpawnHandler() {
        return setSpawnHandler;
    }
}
