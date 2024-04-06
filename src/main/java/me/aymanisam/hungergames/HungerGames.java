package me.aymanisam.hungergames;

import me.aymanisam.hungergames.commands.*;
import me.aymanisam.hungergames.handler.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;


public final class HungerGames extends JavaPlugin {
    public boolean gameStarted = false;
    public BossBar bossBar;
    private GameHandler gameHandler;
    public List<Player> playersAlive;
    private SetSpawnHandler setSpawnHandler;
    private YamlConfiguration langConfig;
    private Map<Player, BossBar> playerBossBars;
    private YamlConfiguration itemsConfig;

    @Override
    public void onEnable() {
        int pluginId = 21508;
        Metrics metrics = new Metrics(this, pluginId);

        this.getLogger().info("This plugin uses bStats to collect anonymous data about servers using this plugin. This data helps improve the plugin and is not shared with third parties.");
        this.getLogger().info("You can disable this data collection in the bStats config.yml file.");

        saveLanguageFiles();
        checkConfigKeys();
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
        File itemsFile = new File(getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            saveResource("items.yml", false);
        }
        Objects.requireNonNull(getCommand("hg")).setExecutor(new CommandHandler(this));
        MoveDisableHandler moveDisableHandler = new MoveDisableHandler(this, chestRefillCommand, playerSignClickManager);
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
        return ChatColor.translateAlternateColorCodes('&', "&c Missing translation for key: " + key + ". For more information on how to update language keys, visit: https://github.com/Ayman-Isam/Hunger-Games/wiki/Language#language-errors ");
    }

    public void loadItemsConfig() {
        File itemsFile = new File(getDataFolder(), "items.yml");
        if (itemsFile.exists()) {
            itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        } else {
            this.getLogger().log(Level.SEVERE, "Items file not found.");
        }
    }

    public void checkConfigKeys() {
        YamlConfiguration pluginConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(this.getResource("config.yml")));
        File serverConfigFile = new File(getDataFolder(), "config.yml");
        YamlConfiguration serverConfig = YamlConfiguration.loadConfiguration(serverConfigFile);
        Set<String> keys = pluginConfig.getKeys(true);

        for (String key : keys) {
            if (!serverConfig.contains(key)) {
                serverConfig.set(key, pluginConfig.get(key));
                System.out.println("&cMissing key: " + key);
            }
        }

        try {
            serverConfig.save(serverConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
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
