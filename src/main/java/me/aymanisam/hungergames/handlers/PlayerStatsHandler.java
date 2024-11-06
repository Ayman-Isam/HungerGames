package me.aymanisam.hungergames.handlers;

import java.sql.Date;

public class PlayerStatsHandler {
    private String uuid;
    private String username;
    private int deaths;
    private int kills;
    private int killAssists;
    private int gamesCreated;
    private int gamesPlayed;
    private int gamesWon;
    private int chestsOpened;
    private int supplyDropsOpened;
    private int environmentDeaths;
    private int borderDeaths;
    private int arrowsShot;
    private int arrowsLanded;
    private int fireworksShot;
    private int attacksBlocked;
    private int healthRegenerated;
    private int potionsConsumed;
    private int foodConsumed;
    private int totemsPopped;
    private double credits;
    private double damageDealt;
    private double projectileDamageDealt;
    private double damageTaken;
    private double projectileDamageTaken;
    private double soloPercentile;
    private double teamPercentile;
    private Date lastLogin;
    private Date lastLogout;
    private Long secondsPlayed;
    private Long secondsPlayedMonth;

    public PlayerStatsHandler(String uuid, String username, int deaths, int kills, int killAssists, int gamesCreated, int gamesPlayed, int gamesWon, int chestsOpened, int supplyDropsOpened, int environmentDeaths, int borderDeaths, int arrowsShot, int arrowsLanded, int fireworksShot, int attacksBlocked, int healthRegenerated, int potionsConsumed, int foodConsumed, int totemsPopped, double credits, double damageDealt, double projectileDamageDealt, double damageTaken, double projectileDamageTaken, double soloPercentile, double teamPercentile, Date lastLogin, Date lastLogout, Long secondsPlayed, Long secondsPlayedMonth) {
        this.uuid = uuid;
        this.username = username;
        this.deaths = deaths;
        this.kills = kills;
        this.killAssists = killAssists;
        this.gamesCreated = gamesCreated;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.chestsOpened = chestsOpened;
        this.supplyDropsOpened = supplyDropsOpened;
        this.environmentDeaths = environmentDeaths;
        this.borderDeaths = borderDeaths;
        this.arrowsShot = arrowsShot;
        this.arrowsLanded = arrowsLanded;
        this.fireworksShot = fireworksShot;
        this.attacksBlocked = attacksBlocked;
        this.healthRegenerated = healthRegenerated;
        this.potionsConsumed = potionsConsumed;
        this.foodConsumed = foodConsumed;
        this.totemsPopped = totemsPopped;
        this.credits = credits;
        this.damageDealt = damageDealt;
        this.projectileDamageDealt = projectileDamageDealt;
        this.damageTaken = damageTaken;
        this.projectileDamageTaken = projectileDamageTaken;
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

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public int getGamesCreated() {
        return gamesCreated;
    }

    public void setGamesCreated(int gamesCreated) {
        this.gamesCreated = gamesCreated;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
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

    public int getAttacksBlocked() {
        return attacksBlocked;
    }

    public void setAttacksBlocked(int attacksBlocked) {
        this.attacksBlocked = attacksBlocked;
    }

    public int getHealthRegenerated() {
        return healthRegenerated;
    }

    public void setHealthRegenerated(int healthRegenerated) {
        this.healthRegenerated = healthRegenerated;
    }

    public int getPotionsConsumed() {
        return potionsConsumed;
    }

    public void setPotionsConsumed(int potionsConsumed) {
        this.potionsConsumed = potionsConsumed;
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

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
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
