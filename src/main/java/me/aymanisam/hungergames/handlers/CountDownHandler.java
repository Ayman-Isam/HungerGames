package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CountDownHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final GameSequenceHandler gameSequenceHandler;

    public CountDownHandler(HungerGames plugin, SetSpawnHandler setSpawnHandler, GameSequenceHandler gameSequenceHandler) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.gameSequenceHandler = gameSequenceHandler;
    }

    public void startCountDown() {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            this.gameSequenceHandler.startGame();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
            HungerGames.gameStarting = false;
        }, 20L * 20);

        countDown("startgame.20-s", 20L * 0);
        countDown("startgame.15-s", 20L * 5);
        countDown("startgame.10-s", 20L * 10);
        countDown("startgame.5-s", 20L * 15);
        countDown("startgame.4-s", 20L * 16);
        countDown("startgame.3-s", 20L * 17);
        countDown("startgame.2-s", 20L * 18);
        countDown("startgame.1-s", 20L * 19);
    }

    private void countDown(String messageKey, long delayInTicks) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                langHandler.getLangConfig(player);
                String message = langHandler.getMessage(messageKey);
                player.sendTitle("", message, 5, 20, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            }
        }, delayInTicks);
    }
}
