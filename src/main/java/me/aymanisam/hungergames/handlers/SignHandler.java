package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static me.aymanisam.hungergames.HungerGames.isGameStartingOrStarted;
import static me.aymanisam.hungergames.commands.SignSetCommand.slots;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class SignHandler {
    private final HungerGames plugin;
	private final SetSpawnHandler setSpawnHandler;
    private final File file;
    private final FileConfiguration config;
	public static final Map<String, Location> signLocations = new HashMap<>();

    public SignHandler(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
		this.setSpawnHandler = setSpawnHandler;
        file = new File(plugin.getDataFolder(), "signs.yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

	public void setSignContent() {
		for (Map.Entry<String, Location> entry : signLocations.entrySet()) {
			String worldName = slots.get(entry.getKey());

			int worldPlayersWaitingSize = setSpawnHandler.playersWaiting.computeIfAbsent(worldName, k -> new ArrayList<>()).size();
			int worldSpawnPointSize = setSpawnHandler.spawnPoints.computeIfAbsent(worldName, k -> new ArrayList<>()).size();
			List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(worldName, k -> new ArrayList<>());

			Location location = entry.getValue();
			if (location.getBlock().getState() instanceof Sign sign) {
				sign.setEditable(false);
				SignSide frontSide = sign.getSide(Side.FRONT);
				SignSide backSide = sign.getSide(Side.BACK);
				frontSide.setLine(0, ChatColor.BOLD + "Join");
				backSide.setLine(0, ChatColor.BOLD + "Join");
				frontSide.setLine(1, ChatColor.BOLD + worldName);
				backSide.setLine(1, ChatColor.BOLD + worldName);
				if (isGameStartingOrStarted(worldName)) {
					frontSide.setLine(2, ChatColor.BOLD + "In Progress");
					backSide.setLine(2, ChatColor.BOLD + "In Progress");
					frontSide.setLine(3, ChatColor.BOLD + "" + worldPlayersAlive.size() + " Alive");
					backSide.setLine(3, ChatColor.BOLD + "" + worldPlayersAlive.size() + " Alive");
				} else {
					frontSide.setLine(2, ChatColor.BOLD + "Waiting");
					backSide.setLine(2, ChatColor.BOLD + "Waiting");
					frontSide.setLine(3, ChatColor.BOLD + "[" + worldPlayersWaitingSize + "/" + worldSpawnPointSize + "]");
					backSide.setLine(3, ChatColor.BOLD + "[" + worldPlayersWaitingSize + "/" + worldSpawnPointSize + "]");
				}
				sign.update();
			}
		}
	}
}
