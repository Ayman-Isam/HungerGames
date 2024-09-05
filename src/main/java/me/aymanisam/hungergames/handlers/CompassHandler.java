package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;

public class CompassHandler {
    private final LangHandler langHandler;

    private final Map<World, Map<Player, Integer>> teammateIndexMap = new HashMap<>();

    public CompassHandler(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
    }

    public Player findNearestTeammate(Player player, Boolean message, World world) {
        List<Player> playerTeam = null;
        List<List<Player>> worldTeams = teams.computeIfAbsent(world, k -> new ArrayList<>());
        Map<Player, Integer> worldTeammateIndexMap = teammateIndexMap.computeIfAbsent(world, k -> new HashMap<>());

        for (List<Player> team : worldTeams) {
            if (team.contains(player)) {
                playerTeam = new ArrayList<>(team);
                playerTeam.remove(player);
                break;
            }
        }

        if (playerTeam == null || playerTeam.isEmpty()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(langHandler.getMessage(player, "arena.compass-nomates")));
            return null;
        }

        Integer index = worldTeammateIndexMap.getOrDefault(player, 0);

        if (player.isSneaking()) {
            index = (index + 1) % playerTeam.size();
            worldTeammateIndexMap.put(player, index);
        }

        int loopCount = 0;
        Player teammate = playerTeam.get(index);

        while (teammate != null && (!teammate.isOnline() || teammate.getGameMode() != GameMode.ADVENTURE || teammate.isDead())) {
            index = (index + 1) % playerTeam.size();
            if (loopCount++ >= playerTeam.size()) {
                teammate = null;
                break;
            }
            worldTeammateIndexMap.put(player, index);
            teammate = playerTeam.get(index);
        }

        worldTeammateIndexMap.put(player, index);

        if (message) {
            if (teammate != null) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(langHandler.getMessage(player, "arena.compass-teammate", teammate.getName())));
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(langHandler.getMessage(player, "arena.compass-nomates")));
            }
        }

        return teammate;
    }

    public Player findNearestEnemy(Player player, Boolean message, World world) {
        double closestDistance = Double.MAX_VALUE;
        Player closestPlayer = null;

        List<Player> playerTeam = null;
        List<List<Player>> worldTeams = teams.computeIfAbsent(world, k -> new ArrayList<>());

        for (List<Player> team : worldTeams) {
            if (team.contains(player)) {
                playerTeam = team;
                break;
            }
        }

        for (Player targetPlayer : player.getWorld().getPlayers()) {
            if (targetPlayer != player && targetPlayer.getGameMode() == GameMode.ADVENTURE && targetPlayer.isOnline() && !(playerTeam == null || playerTeam.contains(targetPlayer))) {
                double distance = player.getLocation().distance(targetPlayer.getLocation());
                if (player.getWorld() != targetPlayer.getWorld()) continue;

                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = targetPlayer;
                }
            }
        }

        if (closestPlayer != null && message) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(langHandler.getMessage(player, "arena.compass-enemy", closestPlayer.getName())));
        }

        return closestPlayer;
    }
}
