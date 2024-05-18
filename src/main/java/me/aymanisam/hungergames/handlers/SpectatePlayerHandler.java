package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.listeners.SpectateGuiListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class SpectatePlayerHandler {
    private final HungerGames plugin;
    private final SpectateGuiListener spectateGuiListener;
    private final LangHandler langHandler;

    private final Map<Integer, Player> slotPlayerMap = new HashMap<>();

    public SpectatePlayerHandler(HungerGames plugin) {
        this.plugin = plugin;
        this.spectateGuiListener = new SpectateGuiListener(plugin);
        this.langHandler = new LangHandler(plugin);
    }

    public void openSpectatorGUI(Player spectator) {
        slotPlayerMap.clear();

        int size = (int) Math.ceil(playersAlive.size() / 9.0) * 9;
        Inventory gui = Bukkit.createInventory(null, size, langHandler.getMessage("spectate.gui-message"));
        for (int i = 0; i < playersAlive.size(); i++) {
            Player player = playersAlive.get(i);
            ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerItem.getItemMeta();
            assert meta != null;
            meta.setOwningPlayer(player);
            meta.setDisplayName(player.getName());
            playerItem.setItemMeta(meta);

            gui.setItem(i, playerItem);
            slotPlayerMap.put(i, player);
        }

        spectator.openInventory(gui);
    }

}
