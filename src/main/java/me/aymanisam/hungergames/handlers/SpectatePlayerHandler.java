package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class SpectatePlayerHandler {
    private final LangHandler langHandler;

    private final Map<Integer, Player> slotPlayerMap = new HashMap<>();

    public SpectatePlayerHandler(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
    }

    public void openSpectatorGUI(Player spectator) {
        slotPlayerMap.clear();

        int size = (int) Math.ceil(playersAlive.size() / 9.0) * 9;
        Inventory gui = Bukkit.createInventory(null, size, langHandler.getMessage(spectator, "spectate.gui-message"));
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
