package me.aymanisam.hungergames;

import me.aymanisam.hungergames.handlers.*;
import me.aymanisam.hungergames.listeners.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Objects;

public final class HungerGames extends JavaPlugin {

    public static boolean gameStarted = false;
    public static boolean gameStarting = false;
    public static World gameWorld;

    private GameSequenceHandler gameSequenceHandler;
    private WorldResetHandler worldresetHandler;

    @Override
    public void onEnable() {
        int pluginId = 21512;
        Metrics metrics = new Metrics(this, pluginId);
        LangHandler langHandler = new LangHandler(this);
        langHandler.saveLanguageFiles();
        langHandler.updateLanguageKeys();
        langHandler.loadLanguageConfigs();

        gameWorld = Bukkit.getWorlds().get(0);

        System.out.println(gameWorld.getName());

        // Initializing shared classes
        TeamVotingListener teamVotingListener = new TeamVotingListener(this);
        getServer().getPluginManager().registerEvents(teamVotingListener, this);
        SetSpawnHandler setSpawnHandler = new SetSpawnHandler(this);
        CompassHandler compassHandler = new CompassHandler(this);
        this.gameSequenceHandler = new GameSequenceHandler(this, setSpawnHandler);
        this.worldresetHandler = new WorldResetHandler(this);
        ArenaHandler arenaHandler = new ArenaHandler(this);
        ConfigHandler configHandler = new ConfigHandler(this);

        // Registering command handler
        Objects.requireNonNull(getCommand("hg")).setExecutor(new CommandDispatcher(this, setSpawnHandler, gameSequenceHandler, teamVotingListener));

        // Registering Listeners
        ArenaSelectListener arenaSelectListener = new ArenaSelectListener(this);
        getServer().getPluginManager().registerEvents(arenaSelectListener, this);

        SetSpawnListener setSpawnListener = new SetSpawnListener(this, setSpawnHandler);
        getServer().getPluginManager().registerEvents(setSpawnListener, this);

        SignClickListener signClickListener = new SignClickListener(this, setSpawnHandler);
        getServer().getPluginManager().registerEvents(signClickListener, this);

        PlayerListener playerListener = new PlayerListener(this, setSpawnHandler);
        getServer().getPluginManager().registerEvents(playerListener, this);

        SpectateGuiListener spectateGuiListener = new SpectateGuiListener(this);
        getServer().getPluginManager().registerEvents(spectateGuiListener, this);

        CompassListener compassListener = new CompassListener(this, compassHandler);
        getServer().getPluginManager().registerEvents(compassListener, this);

        File serverDirectory = new File(".");
        File[] files = serverDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File levelDat = new File(file, "level.dat");
                    if (levelDat.exists()) {
                        String worldName = file.getName();
                        Bukkit.getServer().createWorld(new WorldCreator(worldName));
                    }
                }
            }
        }

        List<World> worlds = getServer().getWorlds();
        for (World world : worlds) {
            String worldName = world.getName();

            if (!worldName.contains("the_end")) {
                File worldFolder = new File(getDataFolder(), worldName);
                if (!worldFolder.exists()) {
                    worldFolder.mkdirs();
                }
                arenaHandler.createArenaConfig(world);
                configHandler.createWorldConfig(world);
                configHandler.loadItemsConfig(world);
            }
        }
    }

    @Override
    public void onDisable() {
        gameSequenceHandler.endGame();
    }

    public File getPluginFile() {
        return this.getFile();
    }
}
