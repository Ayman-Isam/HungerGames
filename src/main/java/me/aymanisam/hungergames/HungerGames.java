package me.aymanisam.hungergames;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.aymanisam.hungergames.handlers.*;
import me.aymanisam.hungergames.listeners.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static me.aymanisam.hungergames.handlers.VersionHandler.getLatestPluginVersion;

public final class HungerGames extends JavaPlugin {

    public static boolean gameStarted = false;
    public static boolean gameStarting = false;
    public static List<String> worldNames = new ArrayList<>();

    private GameSequenceHandler gameSequenceHandler;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .reEncodeByDefault(false)
                .checkForUpdates(true);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        int bstatsPluginId = 21512;
        Metrics metrics = new Metrics(this, bstatsPluginId);
        LangHandler langHandler = new LangHandler(this);
        langHandler.saveLanguageFiles();
        langHandler.updateLanguageKeys();
        langHandler.loadLanguageConfigs();

        // Initializing shared classes
        TeamVotingListener teamVotingListener = new TeamVotingListener(this, langHandler);
        getServer().getPluginManager().registerEvents(teamVotingListener, this);
        SetSpawnHandler setSpawnHandler = new SetSpawnHandler(this, langHandler);
        ScoreBoardHandler scoreBoardHandler = new ScoreBoardHandler(this, langHandler);
        CompassHandler compassHandler = new CompassHandler(this, langHandler);
        CompassListener compassListener = new CompassListener(this, langHandler, compassHandler, scoreBoardHandler);
        ArenaHandler arenaHandler = new ArenaHandler(this, langHandler);
        ConfigHandler configHandler = new ConfigHandler(this, langHandler);
        TeamsHandler teamsHandler = new TeamsHandler(this, langHandler, scoreBoardHandler);
        this.gameSequenceHandler = new GameSequenceHandler(this, langHandler, setSpawnHandler, compassListener, teamsHandler);
        CountDownHandler countDownHandler = new CountDownHandler(this, langHandler, setSpawnHandler, gameSequenceHandler, teamVotingListener, scoreBoardHandler);

        // Registering command handler
        Objects.requireNonNull(getCommand("hg")).setExecutor(new CommandDispatcher(this, langHandler, setSpawnHandler, gameSequenceHandler, teamVotingListener, teamsHandler, scoreBoardHandler, countDownHandler));

        // Registering Listeners
        ArenaSelectListener arenaSelectListener = new ArenaSelectListener(this, langHandler);
        getServer().getPluginManager().registerEvents(arenaSelectListener, this);

        SetSpawnListener setSpawnListener = new SetSpawnListener(this, langHandler, setSpawnHandler);
        getServer().getPluginManager().registerEvents(setSpawnListener, this);

        SignClickListener signClickListener = new SignClickListener(this, langHandler, setSpawnHandler);
        getServer().getPluginManager().registerEvents(signClickListener, this);

        PlayerListener playerListener = new PlayerListener(this, langHandler, setSpawnHandler);
        getServer().getPluginManager().registerEvents(playerListener, this);

        SpectateGuiListener spectateGuiListener = new SpectateGuiListener(this, langHandler);
        getServer().getPluginManager().registerEvents(spectateGuiListener, this);

        getServer().getPluginManager().registerEvents(compassListener, this);

        TeamChatListener teamChatListener = new TeamChatListener(teamsHandler);
        getServer().getPluginManager().registerEvents(teamChatListener, this);

        File serverDirectory = new File(".");
        File[] files = serverDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File levelDat = new File(file, "level.dat");
                    if (levelDat.exists()) {
                        String worldName = file.getName();
                        if (!worldName.contains("the_end")) {
                            worldNames.add(worldName);
                        }
                    }
                }
            }
        }

        System.out.println(worldNames);

        PacketEvents.getAPI().init();

        int spigotPluginId = 111936;

        String latestVersionString = getLatestPluginVersion(spigotPluginId);
        int latestHyphenIndex = latestVersionString.indexOf('-');
        String latestVersion = (latestHyphenIndex != -1) ? latestVersionString.substring(0, latestHyphenIndex) : latestVersionString;

        String currentVersionString = this.getDescription().getVersion();
        int currentHyphenIndex = currentVersionString.indexOf('-');
        String currentVersion = (currentHyphenIndex != -1) ? latestVersionString.substring(0, currentHyphenIndex) : latestVersionString;

        if (latestVersion.equals("Error: null")) {
            this.getLogger().log(Level.WARNING, "Failed to check for updates");
        } else if (!Objects.equals(latestVersion, currentVersion)) {
            this.getLogger().log(Level.WARNING, "You are not running the latest version of HungerGames! ");
            this.getLogger().log(Level.WARNING, "Please update your plugin to the latest version " + "\u001B[36m" + latestVersion + "\u001B[33m" + " for the best experience and bug fixes.");
            this.getLogger().log(Level.WARNING, "https://modrinth.com/plugin/hungergames/versions#all-versions");
        }

        TipsHandler tipsHandler = new TipsHandler(this, langHandler);
        tipsHandler.startSendingTips(600);
    }


    @Override
    public void onDisable() {
        for (World world: Bukkit.getWorlds()) {
            gameSequenceHandler.endGame(true, world);
        }
        PacketEvents.getAPI().terminate();
    }

    public File getPluginFile() {
        return this.getFile();
    }
}
