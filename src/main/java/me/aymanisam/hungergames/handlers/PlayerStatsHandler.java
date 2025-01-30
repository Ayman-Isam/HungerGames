package me.aymanisam.hungergames.handlers;

import java.util.Date;

public class PlayerStatsHandler {
    private final String uuid;
    private String username;
    private int deaths;
    private int kills;
    private int killAssists;
    private int soloGamesStarted;
    private int soloGamesPlayed;
    private int soloGamesWon;
    private int teamGamesStarted;
    private int teamGamesPlayed;
    private int teamGamesWon;
    private int chestsOpened;
    private int supplyDropsOpened;
    private int environmentDeaths;
    private int borderDeaths;
    private int playerDeaths;
    private int arrowsShot;
    private int arrowsLanded;
    private int fireworksShot;
    private int fireworksLanded;
    private int attacksBlocked;
    private int potionsUsed;
    private int foodConsumed;
    private int totemsPopped;
    private double damageDealt;
    private double projectileDamageDealt;
    private double damageTaken;
    private double projectileDamageTaken;
    private double healthRegenerated;
    private double soloPercentile;
    private double teamPercentile;
    private Date lastLogin;
    private Date lastLogout;
    private Long secondsPlayed;
    private Long secondsPlayedMonth;

    public PlayerStatsHandler(String uuid, String username, int deaths, int kills, int killAssists, int soloGamesStarted, int soloGamesPlayed, int soloGamesWon, int teamGamesStarted, int teamGamesPlayed, int teamGamesWon, int chestsOpened, int supplyDropsOpened, int environmentDeaths, int borderDeaths, int playerDeaths, int arrowsShot, int arrowsLanded, int fireworksShot, int fireworksLanded, int attacksBlocked, int potionsUsed, int foodConsumed, int totemsPopped, double damageDealt, double projectileDamageDealt, double damageTaken, double projectileDamageTaken, double healthRegenerated, double soloPercentile, double teamPercentile, Date lastLogin, Date lastLogout, Long secondsPlayed, Long secondsPlayedMonth) {
        this.uuid = uuid;
        this.username = username;
        this.deaths = deaths;
        this.kills = kills;
        this.killAssists = killAssists;
        this.soloGamesStarted = soloGamesStarted;
        this.soloGamesPlayed = soloGamesPlayed;
        this.soloGamesWon = soloGamesWon;
        this.teamGamesStarted = teamGamesStarted;
        this.teamGamesPlayed = teamGamesPlayed;
        this.teamGamesWon = teamGamesWon;
        this.chestsOpened = chestsOpened;
        this.supplyDropsOpened = supplyDropsOpened;
        this.environmentDeaths = environmentDeaths;
        this.borderDeaths = borderDeaths;
        this.playerDeaths = playerDeaths;
        this.arrowsShot = arrowsShot;
        this.arrowsLanded = arrowsLanded;
        this.fireworksShot = fireworksShot;
        this.fireworksLanded = fireworksLanded;
        this.attacksBlocked = attacksBlocked;
        this.potionsUsed = potionsUsed;
        this.foodConsumed = foodConsumed;
        this.totemsPopped = totemsPopped;
        this.damageDealt = damageDealt;
        this.projectileDamageDealt = projectileDamageDealt;
        this.damageTaken = damageTaken;
        this.projectileDamageTaken = projectileDamageTaken;
        this.healthRegenerated = healthRegenerated;
        this.soloPercentile = soloPercentile;
        this.teamPercentile = teamPercentile;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;
        this.secondsPlayed = secondsPlayed;
        this.secondsPlayedMonth = secondsPlayedMonth;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getKillAssists() {
        return killAssists;
    }

    public void setKillAssists(int killAssists) {
        this.killAssists = killAssists;
    }

    public int getSoloGamesStarted() {
        return soloGamesStarted;
    }

    public void setSoloGamesStarted(int soloGamesStarted) {
        this.soloGamesStarted = soloGamesStarted;
    }

    public int getSoloGamesPlayed() {
        return soloGamesPlayed;
    }

    public void setSoloGamesPlayed(int soloGamesPlayed) {
        this.soloGamesPlayed = soloGamesPlayed;
    }

    public int getSoloGamesWon() {
        return soloGamesWon;
    }

    public void setSoloGamesWon(int soloGamesWon) {
        this.soloGamesWon = soloGamesWon;
    }

    public int getTeamGamesStarted() {
        return teamGamesStarted;
    }

    public void setTeamGamesStarted(int teamGamesStarted) {
        this.teamGamesStarted = teamGamesStarted;
    }

    public int getTeamGamesPlayed() {
        return teamGamesPlayed;
    }

    public void setTeamGamesPlayed(int teamGamesPlayed) {
        this.teamGamesPlayed = teamGamesPlayed;
    }

    public int getTeamGamesWon() {
        return teamGamesWon;
    }

    public void setTeamGamesWon(int teamGamesWon) {
        this.teamGamesWon = teamGamesWon;
    }

    public int getChestsOpened() {
        return chestsOpened;
    }

    public void setChestsOpened(int chestsOpened) {
        this.chestsOpened = chestsOpened;
    }

    public int getSupplyDropsOpened() {
        return supplyDropsOpened;
    }

    public void setSupplyDropsOpened(int supplyDropsOpened) {
        this.supplyDropsOpened = supplyDropsOpened;
    }

    public int getEnvironmentDeaths() {
        return environmentDeaths;
    }

    public void setEnvironmentDeaths(int environmentDeaths) {
        this.environmentDeaths = environmentDeaths;
    }

    public int getBorderDeaths() {
        return borderDeaths;
    }

    public void setBorderDeaths(int borderDeaths) {
        this.borderDeaths = borderDeaths;
    }

    public int getPlayerDeaths() {
        return playerDeaths;
    }

    public void setPlayerDeaths(int playerDeaths) {
        this.playerDeaths = playerDeaths;
    }

    public int getArrowsShot() {
        return arrowsShot;
    }

    public void setArrowsShot(int arrowsShot) {
        this.arrowsShot = arrowsShot;
    }

    public int getArrowsLanded() {
        return arrowsLanded;
    }

    public void setArrowsLanded(int arrowsLanded) {
        this.arrowsLanded = arrowsLanded;
    }

    public int getFireworksShot() {
        return fireworksShot;
    }

    public void setFireworksShot(int fireworksShot) {
        this.fireworksShot = fireworksShot;
    }

    public int getFireworksLanded() {
        return fireworksLanded;
    }

    public void setFireworksLanded(int fireworksLanded) {
        this.fireworksLanded = fireworksLanded;
    }

    public int getAttacksBlocked() {
        return attacksBlocked;
    }

    public void setAttacksBlocked(int attacksBlocked) {
        this.attacksBlocked = attacksBlocked;
    }

    public int getPotionsUsed() {
        return potionsUsed;
    }

    public void setPotionsUsed(int potionsUsed) {
        this.potionsUsed = potionsUsed;
    }

    public int getFoodConsumed() {
        return foodConsumed;
    }

    public void setFoodConsumed(int foodConsumed) {
        this.foodConsumed = foodConsumed;
    }

    public int getTotemsPopped() {
        return totemsPopped;
    }

    public void setTotemsPopped(int totemsPopped) {
        this.totemsPopped = totemsPopped;
    }

    public double getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(double damageDealt) {
        this.damageDealt = damageDealt;
    }

    public double getProjectileDamageDealt() {
        return projectileDamageDealt;
    }

    public void setProjectileDamageDealt(double projectileDamageDealt) {
        this.projectileDamageDealt = projectileDamageDealt;
    }

    public double getDamageTaken() {
        return damageTaken;
    }

    public void setDamageTaken(double damageTaken) {
        this.damageTaken = damageTaken;
    }

    public double getProjectileDamageTaken() {
        return projectileDamageTaken;
    }

    public void setProjectileDamageTaken(double projectileDamageTaken) {
        this.projectileDamageTaken = projectileDamageTaken;
    }

    public double getHealthRegenerated() {
        return healthRegenerated;
    }

    public void setHealthRegenerated(double healthRegenerated) {
        this.healthRegenerated = healthRegenerated;
    }

    public double getSoloPercentile() {
        return soloPercentile;
    }

    public void setSoloPercentile(double soloPercentile) {
        this.soloPercentile = soloPercentile;
    }

    public double getTeamPercentile() {
        return teamPercentile;
    }

    public void setTeamPercentile(double teamPercentile) {
        this.teamPercentile = teamPercentile;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(Date lastLogout) {
        this.lastLogout = lastLogout;
    }

    public Long getSecondsPlayed() {
        return secondsPlayed;
    }

    public void setSecondsPlayed(Long secondsPlayed) {
        this.secondsPlayed = secondsPlayed;
    }

    public Long getSecondsPlayedMonth() {
        return secondsPlayedMonth;
    }

    public void setSecondsPlayedMonth(Long secondsPlayedMonth) {
        this.secondsPlayedMonth = secondsPlayedMonth;
    }
}
