package me.aymanisam.hungergames.stats;

import me.aymanisam.hungergames.HungerGames;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import static me.aymanisam.hungergames.HungerGames.statsMap;

public class HungerGamesExpansion extends PlaceholderExpansion {
	private final HungerGames plugin;

	public HungerGamesExpansion(HungerGames plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull String getIdentifier() {
		return "hungergames";
	}

	@Override
	public @NotNull String getAuthor() {
		return String.join(", ", plugin.getDescription().getAuthors());
	}

	@Override
	public @NotNull String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onRequest(OfflinePlayer player, @NotNull String params) {
		if (player == null) return "";

		PlayerStatsHandler playerStats = statsMap.get(player.getUniqueId());

		if (playerStats == null) return "";

		return switch (params.toLowerCase()) {
			case "uuid" -> String.valueOf(playerStats.getUuid());
			case "username" -> String.valueOf(playerStats.getUsername());
			case "deaths" -> String.valueOf(playerStats.getDeaths());
			case "kills" -> String.valueOf(playerStats.getKills());
			case "kill_assists" -> String.valueOf(playerStats.getKillAssists());
			case "solo_games_started" -> String.valueOf(playerStats.getSoloGamesStarted());
			case "solo_games_played" -> String.valueOf(playerStats.getSoloGamesPlayed());
			case "solo_games_won" -> String.valueOf(playerStats.getSoloGamesWon());
			case "team_games_started" -> String.valueOf(playerStats.getTeamGamesStarted());
			case "team_games_played" -> String.valueOf(playerStats.getTeamGamesPlayed());
			case "team_games_won" -> String.valueOf(playerStats.getTeamGamesWon());
			case "chests_opened" -> String.valueOf(playerStats.getChestsOpened());
			case "supply_drops_opened" -> String.valueOf(playerStats.getSupplyDropsOpened());
			case "environment_deaths" -> String.valueOf(playerStats.getEnvironmentDeaths());
			case "border_deaths" -> String.valueOf(playerStats.getBorderDeaths());
			case "player_deaths" -> String.valueOf(playerStats.getPlayerDeaths());
			case "arrows_shot" -> String.valueOf(playerStats.getArrowsShot());
			case "arrows_landed" -> String.valueOf(playerStats.getArrowsLanded());
			case "fireworks_shot" -> String.valueOf(playerStats.getFireworksShot());
			case "fireworks_landed" -> String.valueOf(playerStats.getFireworksLanded());
			case "attacks_blocked" -> String.valueOf(playerStats.getAttacksBlocked());
			case "potions_used" -> String.valueOf(playerStats.getPotionsUsed());
			case "food_consumed" -> String.valueOf(playerStats.getFoodConsumed());
			case "totems_popped" -> String.valueOf(playerStats.getTotemsPopped());
			case "damage_dealt" -> String.format("%.2f", playerStats.getDamageDealt());
			case "projectile_damage_dealt" -> String.format("%.2f", playerStats.getProjectileDamageDealt());
			case "damage_taken" -> String.format("%.2f", playerStats.getDamageTaken());
			case "projectile_damage_taken" -> String.format("%.2f", playerStats.getProjectileDamageTaken());
			case "health_regenerated" -> String.format("%.2f", playerStats.getHealthRegenerated());
			case "solo_percentile" -> String.format("%.2f", playerStats.getSoloPercentile());
			case "team_percentile" -> String.format("%.2f", playerStats.getTeamPercentile());
			case "last_login" -> String.valueOf(playerStats.getLastLogin());
			case "last_logout" -> String.valueOf(playerStats.getLastLogout());
			case "seconds_played" -> String.valueOf(playerStats.getSecondsPlayed());
			case "seconds_played_month" -> String.valueOf(playerStats.getSecondsPlayedMonth());
			default -> "0";
		};
	}
}
