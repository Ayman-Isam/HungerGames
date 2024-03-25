package me.aymanisam.hungergames;

import me.aymanisam.hungergames.commands.*;
import me.aymanisam.hungergames.handler.*;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;


public final class HungerGames extends JavaPlugin {
    public boolean gameStarted = false;
    public BossBar bossBar;
    private GameHandler gameHandler;
    public List<Player> playersAlive;
    private SetSpawnHandler setSpawnHandler;
    private YamlConfiguration langConfig;
    private Map<Player, BossBar> playerBossBars;

    @Override
    public void onEnable() {
        saveLanguageFiles();
        PlayerSignClickManager playerSignClickManager = new PlayerSignClickManager();
        setSpawnHandler = new SetSpawnHandler(this, playerSignClickManager, null);
        JoinGameCommand joinGameCommand = new JoinGameCommand(this, setSpawnHandler);
        setSpawnHandler.setJoinGameCommand(joinGameCommand);
        gameHandler = new GameHandler(this, setSpawnHandler, playerSignClickManager, joinGameCommand);
        getServer().getWorld("world");
        playersAlive = new ArrayList<>();
        new CompassHandler(this);
        ChestRefillCommand chestRefillCommand = new ChestRefillCommand(this);

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
        File itemsFile = new File(getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            saveResource("items.yml", false);
        }
        Objects.requireNonNull(getCommand("supplydrop")).setExecutor(new SupplyDropCommand(this));
        Objects.requireNonNull(getCommand("create")).setExecutor(new ArenaSelectorCommand(this));
        Objects.requireNonNull(getCommand("select")).setExecutor(new ArenaSelectorCommand(this));
        Objects.requireNonNull(getCommand("setspawn")).setExecutor(new SetSpawnCommand(this));
        Objects.requireNonNull(getCommand("join")).setExecutor(joinGameCommand);
        Objects.requireNonNull(getCommand("spectate")).setExecutor(new SpectateCommand(this, this.gameHandler));
        Objects.requireNonNull(getCommand("chestrefill")).setExecutor(new ChestRefillCommand(this));
        Objects.requireNonNull(getCommand("start")).setExecutor(new StartGameCommand(this));
        Objects.requireNonNull(getCommand("end")).setExecutor(new EndGameCommand(this));
        Objects.requireNonNull(getCommand("scanarena")).setExecutor(new ScanArenaCommand(this));
        MoveDisableHandler moveDisableHandler = new MoveDisableHandler(this, chestRefillCommand, playerSignClickManager);
        BorderSetCommand borderSetCommand = new BorderSetCommand(this);
        Objects.requireNonNull(getCommand("border")).setExecutor(borderSetCommand);
        Objects.requireNonNull(getCommand("border")).setTabCompleter(borderSetCommand);
        Objects.requireNonNull(getCommand("reloadconfig")).setExecutor(new ReloadConfigCommand(this));
        getServer().getPluginManager().registerEvents(new SetArenaHandler(this), this);
        getServer().getPluginManager().registerEvents(setSpawnHandler, this);
        getServer().getPluginManager().registerEvents(new WorldBorderHandler(this), this);
        getServer().getPluginManager().registerEvents(gameHandler, this);
        getServer().getPluginManager().registerEvents(moveDisableHandler, this);
        getServer().getPluginManager().registerEvents(new SpectatorTeleportHandler(this, gameHandler), this);
    }

    @Override
    public void onDisable() {
        if (gameHandler != null) {
            gameHandler.endGame();
        }

        if (playersAlive != null) {
            playersAlive.clear();
        }

        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
    }

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public SetSpawnHandler getSetSpawnHandler() {
        return setSpawnHandler;
    }

    public void setBossBar(BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public void saveLanguageFiles() {
        String resourceFolder = "lang";
        File langFolder = new File(getDataFolder(), resourceFolder);
        if (!langFolder.exists()) {
            boolean dirCreated = langFolder.mkdir();
            if (!dirCreated) {
                this.getLogger().log(Level.SEVERE, "Could not create language directory.");
                return;
            }
        }

        for (String lang : new String[]{"en_US", "es_ES", "fr_FR", "hi_IN", "zh_CN", "ar_SA"}) {
            File langFile = new File(langFolder, lang + ".yml");
            if (!langFile.exists()) {
                saveResource(resourceFolder + "/" + lang + ".yml", false);
            }
        }
    }

    public void loadDefaultLanguageConfig() {
        String defaultLocale = "en_US";
        File langFile = new File(getDataFolder(), "lang/" + defaultLocale + ".yml");
        if (langFile.exists()) {
            langConfig = YamlConfiguration.loadConfiguration(langFile);
        }
    }

    public void loadLanguageConfig(Player player) {
        String locale = player.getLocale();
        File langFile = new File(getDataFolder(), "lang/" + locale + ".yml");
        if (langFile.exists()) {
            langConfig = YamlConfiguration.loadConfiguration(langFile);
        } else {
            loadDefaultLanguageConfig();
        }
    }

    public String getMessage(String key) {
        if (langConfig != null) {
            String message = langConfig.getString(key);
            if (message != null) {
                return ChatColor.translateAlternateColorCodes('&', message);
            }
        }
        this.getLogger().log(Level.SEVERE, "Message not found for key: " + key);
        return "Message not found";
    }

    public void loadItemsConfig() {
        File itemsFile = new File(getDataFolder(), "items.yml");
        if (itemsFile.exists()) {
            YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        } else {
            this.getLogger().log(Level.SEVERE, "Items file not found.");
        }
    }

    public void reloadItemsConfig() {
        loadItemsConfig();
    }

    public Map<Player, BossBar> getPlayerBossBars() {
        return playerBossBars;
    }

    public void setPlayerBossBars(Map<Player, BossBar> playerBossBars) {
        this.playerBossBars = playerBossBars;
    }
}
