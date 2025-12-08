package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TipsHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private int tipIndex = 0;

    public TipsHandler(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
    }

    public void sendTips() {
        List<String> tips = new ArrayList<>();
        Set<String> keys = Objects.requireNonNull(langHandler.getLangConfig().getConfigurationSection("tips")).getKeys(false);
        for (String key : keys) {
            String tip = langHandler.getLangConfig().getString("tips." + key);
            tips.add(tip);
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            new BukkitRunnable() {
                int counter = 0;

                @Override
                public void run() {
                    if (counter >= 300 / 30) {
                        this.cancel();
                        return;
                    }
                    if (itemInHand.getItemMeta() == null || !(itemInHand.getItemMeta().getDisplayName().equals(langHandler.getMessage(player, "team.compass-teammate")))) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(langHandler.getMessage(player, "tips." + tipIndex)));
                    }

                    counter++;
                }
            }.runTaskTimer(plugin, 0L, 30L);

        }

        tipIndex = (tipIndex) % tips.size();
    }

    public void startSendingTips(long interval) {
        new BukkitRunnable() {
            @Override
            public void run() {
                sendTips();
            }
        }.runTaskTimer(plugin, 0, interval);
    }
}
