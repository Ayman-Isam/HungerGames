package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;

public class CompassHandler {
    private final HungerGames plugin;
    private final LangHandler langHandler;

    private final Map<String, Map<Player, Integer>> teammateIndexMap = new HashMap<>();

    public CompassHandler(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
    }

    public Player findNearestTeammate(Player player, Boolean message, World world) {
        List<Player> playerTeam = null;
        List<List<Player>> worldTeams = teams.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        Map<Player, Integer> worldTeammateIndexMap = teammateIndexMap.computeIfAbsent(world.getName(), k -> new HashMap<>());

        for (List<Player> team : worldTeams) {
            if (team.contains(player)) {
                playerTeam = new ArrayList<>(team);
                // Tracking teammates except the player
                playerTeam.remove(player);
                break;
            }
        }

        if (playerTeam == null || playerTeam.isEmpty()) {
            return null;
        }

        Integer index = worldTeammateIndexMap.getOrDefault(player, 0);

        int loopCount = 0;
        Player teammate = playerTeam.get(index);

        while (teammate != null && (!teammate.isOnline() || teammate.getGameMode() != GameMode.ADVENTURE || teammate.isDead())) {
            // Putting teammates into worldTeammateIndexMap
            index = (index + 1) % playerTeam.size();
            if (loopCount++ >= playerTeam.size()) {
                teammate = null;
                break;
            }
            worldTeammateIndexMap.put(player, index);
            teammate = playerTeam.get(index);
        }

        worldTeammateIndexMap.put(player, index);

        if (player.isSneaking()) {
            index = (index + 1) % playerTeam.size();
            // Iterating over the teammate to be tracked
            worldTeammateIndexMap.put(player, index);
        }

        if (message) {
            if (teammate != null) {
                plugin.adventure().player(player).sendActionBar(Component.text(langHandler.getMessage(player, "arena.compass-teammate", teammate.getName())));
            } else {
                plugin.adventure().player(player).sendActionBar(Component.text(langHandler.getMessage(player, "arena.compass-nomates")));
            }
        }

        return teammate;
    }

    public Player findNearestEnemy(Player player, Boolean message, World world) {
        double closestDistance = Double.MAX_VALUE;
        Player closestPlayer = null;

        List<Player> playerTeam = null;
        List<List<Player>> worldTeams = teams.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        for (List<Player> team : worldTeams) {
            if (team.contains(player)) {
                playerTeam = team;
                break;
            }
        }

        for (Player targetPlayer : player.getWorld().getPlayers()) {
            if (targetPlayer != player && (targetPlayer.getGameMode() == GameMode.ADVENTURE || targetPlayer.getGameMode() == GameMode.SURVIVAL) && targetPlayer.isOnline() && !(playerTeam == null || playerTeam.contains(targetPlayer))) {
                double distance = player.getLocation().distance(targetPlayer.getLocation());
                if (player.getWorld() != targetPlayer.getWorld()) continue;

                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = targetPlayer;
                }
            }
        }

        if (closestPlayer != null && message) {
            plugin.adventure().player(player).sendActionBar(Component.text(langHandler.getMessage(player, "arena.compass-enemy", closestPlayer.getName())));
        }

        return closestPlayer;
    }
}
