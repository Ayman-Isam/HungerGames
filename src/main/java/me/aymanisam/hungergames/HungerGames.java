package me.aymanisam.hungergames;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.aymanisam.hungergames.handlers.*;
import me.aymanisam.hungergames.listeners.*;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static me.aymanisam.hungergames.handlers.VersionHandler.getLatestPluginVersion;

public final class HungerGames extends JavaPlugin {
    public static Map<World, Boolean> gameStarted = new HashMap<>();
    public static Map<World, Boolean> gameStarting = new HashMap<>();
    public static List<String> worldNames = new ArrayList<>();

    private GameSequenceHandler gameSequenceHandler;
    private ConfigHandler configHandler;
    private DatabaseHandler database;
    private BukkitAudiences adventure;

    @Override
    public void onLoad() {
        // PacketEvents code
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .reEncodeByDefault(false)
                .checkForUpdates(true);
        PacketEvents.getAPI().load();
    }

    public DatabaseHandler getDatabase() {
        return database;
    }

    public @NonNull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @Override
    public void onEnable() {
        // Bstats
        int bstatsPluginId = 21512;
        Metrics metrics = new Metrics(this, bstatsPluginId);

        // Database
        try {
            this.database = new DatabaseHandler(this);
            database.initializeDatabase();
        } catch (SQLException e) {
            System.out.println("Unable to connect to database and create tables.");
            e.printStackTrace();
        }

        // Adventure
        this.adventure = BukkitAudiences.create(this);

        LangHandler langHandler = new LangHandler(this);
        langHandler.saveLanguageFiles();
        langHandler.validateLanguageKeys();
        langHandler.loadLanguageConfigs();

        // Initializing shared classes
        this.configHandler = new ConfigHandler(this, langHandler);
        TeamVotingListener teamVotingListener = new TeamVotingListener(this, langHandler);
        getServer().getPluginManager().registerEvents(teamVotingListener, this);
        ArenaHandler arenaHandler = new ArenaHandler(this, langHandler);
        SetSpawnHandler setSpawnHandler = new SetSpawnHandler(this, langHandler, arenaHandler);
        ScoreBoardHandler scoreBoardHandler = new ScoreBoardHandler(this, langHandler);
        CompassHandler compassHandler = new CompassHandler(this, langHandler);
        CompassListener compassListener = new CompassListener(this, langHandler, compassHandler, scoreBoardHandler);
        TeamsHandler teamsHandler = new TeamsHandler(this, langHandler, scoreBoardHandler);
        this.gameSequenceHandler = new GameSequenceHandler(this, langHandler, setSpawnHandler, compassListener, teamsHandler);
        CountDownHandler countDownHandler = new CountDownHandler(this, langHandler, setSpawnHandler, gameSequenceHandler, teamVotingListener, scoreBoardHandler);
        setSpawnHandler.setCountDownHandler(countDownHandler);

        // Registering command handler
        Objects.requireNonNull(getCommand("hg")).setExecutor(new CommandDispatcher(this, langHandler, setSpawnHandler, gameSequenceHandler, teamVotingListener, teamsHandler, scoreBoardHandler, countDownHandler, arenaHandler));

        // Registering Listeners
        ArenaSelectListener arenaSelectListener = new ArenaSelectListener(this, langHandler);
        getServer().getPluginManager().registerEvents(arenaSelectListener, this);

        SetSpawnListener setSpawnListener = new SetSpawnListener(this, langHandler, setSpawnHandler, arenaHandler);
        getServer().getPluginManager().registerEvents(setSpawnListener, this);

        SignClickListener signClickListener = new SignClickListener(this, langHandler, setSpawnHandler, arenaHandler);
        getServer().getPluginManager().registerEvents(signClickListener, this);

        PlayerListener playerListener = new PlayerListener(this, langHandler, setSpawnHandler, scoreBoardHandler);
        getServer().getPluginManager().registerEvents(playerListener, this);

        SpectateGuiListener spectateGuiListener = new SpectateGuiListener(this, langHandler);
        getServer().getPluginManager().registerEvents(spectateGuiListener, this);

        getServer().getPluginManager().registerEvents(compassListener, this);

        TeamChatListener teamChatListener = new TeamChatListener(teamsHandler);
        getServer().getPluginManager().registerEvents(teamChatListener, this);

        File serverDirectory = new File(".");
        File[] files = serverDirectory.listFiles();

        // Checking Files for World Files
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File levelDat = new File(file, "level.dat");
                    if (levelDat.exists()) {
                        String worldName = file.getName();
                        if (!configHandler.createPluginSettings().getStringList("ignored-worlds").contains(worldName)) {
                            worldNames.add(worldName);
                        }
                    }
                }
            }
        }

        configHandler.createPluginSettings();
        configHandler.validateSettingsKeys();

        PacketEvents.getAPI().init();

        // Checks if the current version is the latest version
        int spigotPluginId = 111936;

        String latestVersionString = getLatestPluginVersion(spigotPluginId);
        int latestHyphenIndex = latestVersionString.indexOf('-');
        String latestVersion = (latestHyphenIndex != -1) ? latestVersionString.substring(0, latestHyphenIndex) : latestVersionString;

        String currentVersionString = this.getDescription().getVersion();
        int currentHyphenIndex = currentVersionString.indexOf('-');
        String currentVersion = (currentHyphenIndex != -1) ? currentVersionString.substring(0, currentHyphenIndex) : currentVersionString;

        if (latestVersion.equals("Error: null")) {
            this.getLogger().log(Level.WARNING, "Failed to check for updates");
        } else if (!Objects.equals(latestVersion, currentVersion)) {
            this.getLogger().log(Level.WARNING, "You are not running the latest version of HungerGames! ");
            this.getLogger().log(Level.WARNING, "Please update your plugin to the latest version " + "\u001B[36m" + latestVersion + "\u001B[33m" + " for the best experience and bug fixes.");
            this.getLogger().log(Level.WARNING, "https://modrinth.com/plugin/hungergames/versions#all-versions");
        }

        TipsHandler tipsHandler = new TipsHandler(this, langHandler);
        if (configHandler.createPluginSettings().getBoolean("tips")) {
            tipsHandler.startSendingTips(600);
        }

        configHandler.loadSignLocations();
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    @Override
    public void onDisable() {
        for (World world: Bukkit.getWorlds()) {
            gameSequenceHandler.endGame(true, world);
        }

        PacketEvents.getAPI().terminate();

        this.database.closeConnection();

        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public File getPluginFile() {
        return this.getFile();
    }

    public static boolean isGameStartingOrStarted(World world) {
        return gameStarted.getOrDefault(world, false) ||
                gameStarting.getOrDefault(world, false);
    }
}
