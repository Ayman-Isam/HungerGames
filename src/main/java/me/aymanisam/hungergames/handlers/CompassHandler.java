package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;

public class CompassHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;

    private final Map<Player, Integer> teammateIndexMap = new HashMap<>();

    public CompassHandler (HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
    }

    public Player findNearestTeammate(Player player) {
        List<Player> playerTeam = null;
        for (List<Player> team : teams) {
            if (team.contains(player)) {
                playerTeam = new ArrayList<>(team);
                playerTeam.remove(player);
                break;
            }
        }

        if (playerTeam == null || playerTeam.size() == 1) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(langHandler.getMessage("arena.compass-nomates")));
            return null;
        }

        Integer index = teammateIndexMap.getOrDefault(player, 0);

        if (player.isSneaking()) {
            index = (index + 1) % playerTeam.size();
            teammateIndexMap.put(player, index);
        }

        int initialIndex = index;
        int loopCount = 0;
        Player teammate = playerTeam.get(index);

        while (teammate != null && (!teammate.isOnline() || teammate.getGameMode() != GameMode.ADVENTURE || teammate.isDead())) {
            index = (index + 1) % playerTeam.size();
            if (loopCount++ >= playerTeam.size()) {
                teammate = null;
                break;
            }
            teammateIndexMap.put(player, index);
            teammate = playerTeam.get(index);
        }

        teammateIndexMap.put(player, index);

        if (teammate != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(langHandler.getMessage("arena.compass-teammate") + teammate.getName()));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(langHandler.getMessage("arena.compass-nomates")));
        }

        return teammate;
    }

    public Player findNearestEnemy(Player player){
        double closestDistance = Double.MAX_VALUE;
        Player closestPlayer = null;

        List<Player> playerTeam = null;
        for (List<Player> team : teams) {
            if (team.contains(player)) {
                playerTeam = team;
                break;
            }
        }

        for (Player targetPlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (targetPlayer != player && targetPlayer.getGameMode() == GameMode.ADVENTURE && targetPlayer.isOnline() && !(playerTeam == null || playerTeam.contains(targetPlayer))) {
                double distance = player.getLocation().distance(targetPlayer.getLocation());
                if (player.getWorld() != targetPlayer.getWorld()) continue;

                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = targetPlayer;
                }
            }
        }

        if (closestPlayer != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(langHandler.getMessage("arena.compass-enemy") + closestPlayer.getName()));
        }

        return closestPlayer;
    }
}
