package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.*;
import me.aymanisam.hungergames.stats.DatabaseHandler;
import me.aymanisam.hungergames.stats.DisplayStatsHandler;
import me.aymanisam.hungergames.stats.PlayerStatsHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

import static me.aymanisam.hungergames.HungerGames.statsMap;

public class DisplayStatsCommand implements CommandExecutor {
	private final LangHandler langHandler;
	private final ConfigHandler configHandler;
	private final DisplayStatsHandler displayStatsHandler;

	public DisplayStatsCommand(HungerGames plugin, LangHandler langHandler) {
		this.langHandler = langHandler;
		this.configHandler = plugin.getConfigHandler();
		this.displayStatsHandler = new DisplayStatsHandler(plugin, langHandler);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(langHandler.getMessage(null, "no-server"));
			return true;
		}

		if (!player.hasPermission("hungergames.stats")) {
			player.sendMessage(langHandler.getMessage(player, "no-permission"));
			return true;
		}

		if (!configHandler.getPluginSettings().getBoolean("database.enabled")) {
			player.sendMessage(langHandler.getMessage(player, "no-database"));
			return true;
		}

		displayStatsHandler.displayPlayerHead(player);

		PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

		DecimalFormat df = new DecimalFormat("#.##");

		player.sendMessage(langHandler.getMessage(player, "stats.header", player.getName()));
		player.sendMessage(langHandler.getMessage(player, "stats.separator"));
		player.sendMessage(langHandler.getMessage(player, "stats.general-title"));
		player.sendMessage(langHandler.getMessage(player, "stats.general-1",
				playerStats.getSoloGamesStarted() + playerStats.getTeamGamesStarted(),
				playerStats.getSoloGamesPlayed() + playerStats.getTeamGamesPlayed(),
				playerStats.getSoloGamesWon() + playerStats.getTeamGamesWon()));
		double kdRatio = playerStats.getDeaths() > 0 ? (double) playerStats.getKills() / playerStats.getDeaths() : 0;
		player.sendMessage(langHandler.getMessage(player, "stats.general-2", playerStats.getKills(),
				playerStats.getDeaths(), playerStats.getKillAssists(), kdRatio));
		player.sendMessage(langHandler.getMessage(player, "stats.general-3",
				df.format(playerStats.getSecondsPlayed() / 3600), playerStats.getLastLogout()));
		player.sendMessage(langHandler.getMessage(player, "stats.solo-title"));
		player.sendMessage(langHandler.getMessage(player, "stats.solo-1", playerStats.getSoloGamesStarted(),
				playerStats.getSoloGamesPlayed(), playerStats.getSoloGamesWon()));
		player.sendMessage(langHandler.getMessage(player, "stats.solo-2", df.format(playerStats.getSoloPercentile())));
		player.sendMessage(langHandler.getMessage(player, "stats.team-title"));
		player.sendMessage(langHandler.getMessage(player, "stats.team-1", playerStats.getTeamGamesStarted(),
				playerStats.getTeamGamesPlayed(), playerStats.getTeamGamesWon()));
		player.sendMessage(langHandler.getMessage(player, "stats.team-2", df.format(playerStats.getTeamPercentile())));
		player.sendMessage(langHandler.getMessage(player, "stats.death-title"));
		player.sendMessage(langHandler.getMessage(player, "stats.death-1", playerStats.getPlayerDeaths(),
				playerStats.getEnvironmentDeaths(), playerStats.getBorderDeaths()));
		player.sendMessage(langHandler.getMessage(player, "stats.death-2", playerStats.getDeaths()));
		player.sendMessage(langHandler.getMessage(player, "stats.survival-title"));
		player.sendMessage(langHandler.getMessage(player, "stats.survival-1", playerStats.getChestsOpened(),
				playerStats.getSupplyDropsOpened()));
		player.sendMessage(langHandler.getMessage(player, "stats.survival-2", playerStats.getPotionsUsed(),
				playerStats.getFoodConsumed()));
		player.sendMessage(langHandler.getMessage(player, "stats.survival-3", playerStats.getTotemsPopped(),
				df.format(playerStats.getHealthRegenerated())));
		player.sendMessage(langHandler.getMessage(player, "stats.combat-title"));
		player.sendMessage(langHandler.getMessage(player, "stats.combat-1", playerStats.getArrowsShot(),
				playerStats.getArrowsLanded()));
		player.sendMessage(langHandler.getMessage(player, "stats.combat-2", playerStats.getFireworksShot(),
				playerStats.getFireworksLanded()));
		player.sendMessage(langHandler.getMessage(player, "stats.combat-3", playerStats.getAttacksBlocked()));
		player.sendMessage(langHandler.getMessage(player, "stats.combat-4", df.format(playerStats.getDamageDealt()),
				df.format(playerStats.getProjectileDamageDealt())));
		player.sendMessage(langHandler.getMessage(player, "stats.combat-5", df.format(playerStats.getDamageTaken()),
				df.format(playerStats.getProjectileDamageTaken())));
		player.sendMessage(langHandler.getMessage(player, "stats.footer"));

		return true;
	}
}
