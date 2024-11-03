package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class SpectatePlayerHandler {
    private final LangHandler langHandler;

    public SpectatePlayerHandler(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
    }

    public void openSpectatorGUI(Player spectator) {
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(spectator.getWorld(), k -> new ArrayList<>());

        int size = (int) Math.ceil(worldPlayersAlive.size() / 9.0) * 9;
        Inventory gui = Bukkit.createInventory(null, size, langHandler.getMessage(spectator, "spectate.gui-message"));
        for (int i = 0; i < worldPlayersAlive.size(); i++) {
            Player player = worldPlayersAlive.get(i);
            ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerItem.getItemMeta();
            assert meta != null;
            meta.setOwningPlayer(player);
            meta.setDisplayName(player.getName());
            playerItem.setItemMeta(meta);

            gui.setItem(i, playerItem);
        }

        spectator.openInventory(gui);
    }

}
