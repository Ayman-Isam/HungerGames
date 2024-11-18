package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.entity.Player;


import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.logging.Level;

public class DatabaseHandler {
    private final HungerGames plugin;
    private final ConfigHandler configHandler;

    private Connection connection;

    public DatabaseHandler (HungerGames plugin) {
        this.plugin = plugin;
        LangHandler langHandler = new LangHandler(plugin);
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    public Connection getConnection() throws SQLException {
        if (connection != null) {
            return connection;
        }

        String url = configHandler.createPluginSettings().getString("database-url");
        String user = configHandler.createPluginSettings().getString("database-user");
        String password = configHandler.createPluginSettings().getString("database-password");

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, e.toString());
        }

        assert url != null;
        this.connection = DriverManager.getConnection(url, user, password);

        plugin.getLogger().log(Level.CONFIG, "Connected to HungerGames Database");

        return this.connection;
    }

    public void initializeDatabase() throws SQLException {
        Statement statement = getConnection().createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS player_stats(uuid char(36) primary key, username varchar(16), deaths int, kills int, killAssists int, soloGamesCreated int, soloGamesPlayed int, soloGamesWon int, teamGamesCreated int, teamGamesPlayed int, teamGamesWon int, chestsOpened int, supplyDropsOpened int, environmentDeaths int, borderDeaths int, playerDeaths int, arrowsShot int, arrowsLanded int, fireworksShot int, fireworksLanded int, attacksBlocked int, potionsUsed int, foodConsumed int, totemsPopped int, credits double, damageDealt double, projectileDamageDealt double, damageTaken double, projectileDamageTaken double, healthRegenerated double, soloPercentile double, teamPercentile double, lastLogin DATE, lastLogout DATE, secondsPlayed LONG)";
        statement.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS player_monthly_playtime(uuid char(36) primary key, username varchar(16))";
        statement.execute(sql);

        statement.close();

        plugin.getLogger().log(Level.CONFIG, "Created the stats table in the database.");
    }

    public PlayerStatsHandler getPlayerStatsFromDatabase(Player player) throws SQLException {
        PlayerStatsHandler stats = this.plugin.getDatabase().findPlayerStatsByUUID(player.getUniqueId().toString());

        if (stats == null) {
            stats = new PlayerStatsHandler(player.getUniqueId().toString(), player.getName(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0,0,0.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, new java.util.Date(), new java.util.Date(), 0L, 0L);
            this.plugin.getDatabase().createPlayerStats(stats);
        }

        return stats;
    }

    public PlayerStatsHandler findPlayerStatsByUUID(String uuid) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM player_stats WHERE uuid = ?");
        statement.setString(1, uuid);
        ResultSet results = statement.executeQuery();

        String monthYear = LocalDate.now().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toLowerCase() + "_" + LocalDate.now().getYear();
        ResultSet monthResults;


        try {
            PreparedStatement monthStatement = getConnection().prepareStatement("SELECT " + monthYear + " FROM player_monthly_playtime WHERE uuid = ?");
            monthStatement.setString(1, uuid);
            monthResults = monthStatement.executeQuery();
        } catch (SQLSyntaxErrorException e) {
            addMonthColumn(monthYear);
            PreparedStatement monthStatement = getConnection().prepareStatement("SELECT " + monthYear + " FROM player_monthly_playtime WHERE uuid = ?");
            monthStatement.setString(1, uuid);
            monthResults = monthStatement.executeQuery();
        }

        if (results.next()) {
            String username = results.getString("username");
            int deaths = results.getInt("deaths");
            int kills = results.getInt("kills");
            int killAssists = results.getInt("killAssists");
            int soloGamesCreated = results.getInt("soloGamesCreated");
            int soloGamesPlayed = results.getInt("soloGamesPlayed");
            int soloGamesWon = results.getInt("soloGamesWon");
            int teamGamesCreated = results.getInt("teamGamesCreated");
            int teamGamesPlayed = results.getInt("teamGamesPlayed");
            int teamGamesWon = results.getInt("teamGamesWon");
            int chestsOpened = results.getInt("chestsOpened");
            int supplyDropsOpened = results.getInt("supplyDropsOpened");
            int environmentDeaths = results.getInt("environmentDeaths");
            int borderDeaths = results.getInt("borderDeaths");
            int playerDeaths = results.getInt("playerDeaths");
            int arrowsShot= results.getInt("arrowsShot");
            int arrowsLanded = results.getInt("arrowsLanded");
            int fireworksShot = results.getInt("fireworksShot");
            int fireworksLanded = results.getInt("fireworksLanded");
            int attacksBlocked = results.getInt("attacksBlocked");
            int potionsUsed = results.getInt("potionsUsed");
            int foodConsumed = results.getInt("foodConsumed");
            int totemsPopped = results.getInt("totemsPopped");
            double credits = results.getLong("credits");
            double damageDealt = results.getDouble("damageDealt");
            double projectileDamageDealt = results.getDouble("projectileDamageDealt");
            double damageTaken = results.getDouble("damageTaken");
            double projectileDamageTaken = results.getDouble("projectileDamageTaken");
            double healthRegenerated = results.getInt("healthRegenerated");
            double soloPercentile = results.getDouble("soloPercentile");
            double teamPercentile = results.getDouble("teamPercentile");
            Date lastLogin = results.getDate("lastLogin");
            Date lastLogout = results.getDate("lastLogout");
            long secondsPlayed = results.getLong("secondsPlayed");
            long secondsPlayedMonth = 0L;
            if (monthResults.next()) {
                secondsPlayedMonth = monthResults.getLong(monthYear);
            }

            PlayerStatsHandler playerStats = new PlayerStatsHandler(uuid, username, deaths, kills, killAssists, soloGamesCreated, soloGamesPlayed, soloGamesWon, teamGamesCreated, teamGamesPlayed, teamGamesWon, chestsOpened, supplyDropsOpened, environmentDeaths, borderDeaths, playerDeaths, arrowsShot, arrowsLanded, fireworksShot, fireworksLanded, attacksBlocked, potionsUsed, foodConsumed, totemsPopped, credits, damageDealt, projectileDamageDealt, damageTaken, projectileDamageTaken, healthRegenerated, soloPercentile, teamPercentile, lastLogin, lastLogout, secondsPlayed, secondsPlayedMonth);

            statement.close();

            return playerStats;
        } else {
            statement.close();
            return null;
        }
    }

    public void createPlayerStats(PlayerStatsHandler stats) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("INSERT INTO player_stats(uuid, username, deaths, kills, killAssists, soloGamesCreated, soloGamesPlayed, soloGamesWon, teamGamesCreated, teamGamesPlayed, teamGamesWon, chestsOpened, supplyDropsOpened, environmentDeaths, borderDeaths, playerDeaths, arrowsShot, arrowsLanded, fireworksShot, fireworksLanded, attacksBlocked, potionsUsed, foodConsumed, totemsPopped, credits, damageDealt, projectileDamageDealt, damageTaken, projectileDamageTaken, healthRegenerated, soloPercentile, teamPercentile, lastLogin, lastLogout, secondsPlayed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setString(1, stats.getUuid());
        statement.setString(2, stats.getUsername());
        statement.setInt(3, stats.getDeaths());
        statement.setInt(4, stats.getKills());
        statement.setInt(5, stats.getKillAssists());
        statement.setInt(6, stats.getSoloGamesCreated());
        statement.setInt(7, stats.getSoloGamesPlayed());
        statement.setInt(8, stats.getSoloGamesWon());
        statement.setInt(9, stats.getTeamGamesCreated());
        statement.setInt(10, stats.getTeamGamesPlayed());
        statement.setInt(11, stats.getTeamGamesWon());
        statement.setInt(12, stats.getChestsOpened());
        statement.setInt(13, stats.getSupplyDropsOpened());
        statement.setInt(14, stats.getEnvironmentDeaths());
        statement.setInt(15, stats.getBorderDeaths());
        statement.setInt(16, stats.getBorderDeaths());
        statement.setInt(17, stats.getArrowsShot());
        statement.setInt(18, stats.getArrowsLanded());
        statement.setInt(19, stats.getFireworksShot());
        statement.setInt(20, stats.getFireworksLanded());
        statement.setInt(21, stats.getAttacksBlocked());
        statement.setInt(22, stats.getPotionsUsed());
        statement.setInt(23, stats.getFoodConsumed());
        statement.setInt(24, stats.getTotemsPopped());
        statement.setDouble(25, stats.getCredits());
        statement.setDouble(26, stats.getDamageDealt());
        statement.setDouble(27, stats.getProjectileDamageDealt());
        statement.setDouble(28, stats.getDamageTaken());
        statement.setDouble(29, stats.getProjectileDamageTaken());
        statement.setDouble(30, stats.getHealthRegenerated());
        statement.setDouble(31, stats.getSoloPercentile());
        statement.setDouble(32, stats.getTeamPercentile());
        statement.setDate(33, new Date(stats.getLastLogin().getTime()));
        statement.setDate(34, new Date(stats.getLastLogout().getTime()));
        statement.setLong(35, stats.getSecondsPlayed());

        statement.executeUpdate();
        statement.close();

        String monthYear = LocalDate.now().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toLowerCase() + "_" + LocalDate.now().getYear();
        PreparedStatement monthStatement = getConnection().prepareStatement("INSERT INTO player_monthly_playtime (uuid, username, " + monthYear + ") VALUES (?, ?, ?)");
        monthStatement.setString(1, stats.getUuid());
        monthStatement.setString(2, stats.getUsername());
        monthStatement.setLong(3, stats.getSecondsPlayedMonth());

        monthStatement.executeUpdate();
        monthStatement.close();
    }

    public void updatePlayerStats(PlayerStatsHandler stats) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("UPDATE player_stats SET username = ?, deaths = ?, kills = ?, killAssists = ?, soloGamesCreated = ?, soloGamesPlayed = ?, soloGamesWon = ?, teamGamesCreated = ?, teamGamesPlayed = ?, teamGamesWon = ?, chestsOpened = ?, supplyDropsOpened = ?, environmentDeaths = ?, borderDeaths = ?, playerDeaths = ?, arrowsShot = ?, arrowsLanded = ?, fireworksShot = ?, fireworksLanded = ?, attacksBlocked = ?, potionsUsed = ?, foodConsumed = ?, totemsPopped = ?, credits = ?, damageDealt = ?, projectileDamageDealt = ?, damageTaken = ?, projectileDamageTaken = ?, healthRegenerated = ?, soloPercentile = ?, teamPercentile = ?, lastLogin = ?, lastLogout = ?, secondsPlayed = ? WHERE uuid = ?");
        statement.setString(1, stats.getUsername());
        statement.setInt(2, stats.getDeaths());
        statement.setInt(3, stats.getKills());
        statement.setInt(4, stats.getKillAssists());
        statement.setInt(5, stats.getSoloGamesCreated());
        statement.setInt(6, stats.getSoloGamesPlayed());
        statement.setInt(7, stats.getSoloGamesWon());
        statement.setInt(8, stats.getTeamGamesCreated());
        statement.setInt(9, stats.getTeamGamesPlayed());
        statement.setInt(10, stats.getTeamGamesWon());
        statement.setInt(11, stats.getChestsOpened());
        statement.setInt(12, stats.getSupplyDropsOpened());
        statement.setInt(13, stats.getEnvironmentDeaths());
        statement.setInt(14, stats.getBorderDeaths());
        statement.setInt(15, stats.getPlayerDeaths());
        statement.setInt(16, stats.getArrowsShot());
        statement.setInt(17, stats.getArrowsLanded());
        statement.setInt(18, stats.getFireworksShot());
        statement.setInt(19, stats.getFireworksLanded());
        statement.setInt(20, stats.getAttacksBlocked());
        statement.setInt(21, stats.getPotionsUsed());
        statement.setInt(22, stats.getFoodConsumed());
        statement.setInt(23, stats.getTotemsPopped());
        statement.setDouble(24, stats.getCredits());
        statement.setDouble(25, stats.getDamageDealt());
        statement.setDouble(26, stats.getProjectileDamageDealt());
        statement.setDouble(27, stats.getDamageTaken());
        statement.setDouble(28, stats.getProjectileDamageTaken());
        statement.setDouble(29, stats.getHealthRegenerated());
        statement.setDouble(30, stats.getSoloPercentile());
        statement.setDouble(31, stats.getTeamPercentile());
        statement.setDate(32, new Date(stats.getLastLogin().getTime()));
        statement.setDate(33, new Date(stats.getLastLogout().getTime()));
        statement.setLong(34, stats.getSecondsPlayed());
        statement.setString(35, stats.getUuid());

        statement.executeUpdate();
        statement.close();

        String monthYear = LocalDate.now().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toLowerCase() + "_" + LocalDate.now().getYear();
        PreparedStatement monthStatement = getConnection().prepareStatement("UPDATE player_monthly_playtime SET " + monthYear + " = ? WHERE uuid = ?");
        monthStatement.setLong(1, stats.getSecondsPlayedMonth());
        monthStatement.setString(2, stats.getUuid());

        monthStatement.executeUpdate();
        monthStatement.close();
    }

    public void deletePlayerStats(String uuid) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM player_stats WHERE uuid = ?");
        statement.setString(1, uuid);
        statement.executeUpdate();
        statement.close();

        statement = getConnection().prepareStatement("DELETE FROM player_monthly_playtime WHERE uuid = ?");
        statement.setString(1, uuid);
        statement.executeUpdate();
        statement.close();
    }

    public void addMonthColumn(String monthYear) throws SQLException {
        String sql = "ALTER TABLE player_monthly_playtime ADD COLUMN " + monthYear + " INT DEFAULT 0";
        Statement statement = getConnection().createStatement();
        statement.execute(sql);
        statement.close();
    }

    public void closeConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, e.toString());
            }
        }
    }
}
