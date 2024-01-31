package me.cantankerousally.hungergames.handler;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class PlayerSignClickManager {
    private final Map<Player, Boolean> playerSignClicked = new HashMap<>();

    public void setPlayerSignClicked(Player player, boolean clicked) {
        playerSignClicked.put(player, clicked);
    }

    public boolean hasPlayerClickedSign(Player player) {
        return playerSignClicked.getOrDefault(player, false);
    }

    public void removePlayerSignClicked(Player player) {
        playerSignClicked.remove(player);
    }
}
