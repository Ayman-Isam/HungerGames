package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.customTeams;
import static me.aymanisam.hungergames.HungerGames.teamsFinalized;

public class TeamSetCommand implements CommandExecutor {
    private final LangHandler langHandler;

    public TeamSetCommand(LangHandler langHandler) {
        this.langHandler = langHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(player.hasPermission("hungergames.team"))) {
            sender.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "team.no-args"));
            return true;
        }

        String action = args[0];

        if (action.equalsIgnoreCase("list")) {
            for (Map.Entry<String, List<Player>> entry : customTeams.entrySet()) {
                String team = entry.getKey();
                List<Player> members = entry.getValue();
                List<String> memberNames = members.stream().map(Player::getName).collect(Collectors.toList());
                sender.sendMessage(team + ": " + String.join(", ", memberNames));
            }
            if (customTeams.isEmpty()) {
                sender.sendMessage(langHandler.getMessage((Player) sender, "team.no-list"));
            }
            return true;
        } else if (action.equalsIgnoreCase("reset")) {
            customTeams.clear();
            teamsFinalized = false;
            sender.sendMessage(langHandler.getMessage((Player) sender, "team.reset"));
            return true;
        } else if (action.equalsIgnoreCase("finalize")) {
            teamsFinalized = true;
            sender.sendMessage(langHandler.getMessage((Player) sender, "team.finalize"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "team.no-args"));
            return true;
        }

        String teamName = args[1];
        String playerName = args[2];

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null || targetPlayer.getWorld() != player.getWorld()) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "spectate.null-player"));
            return true;
        }

        if (action.equalsIgnoreCase("add")) {
            if (teamsFinalized) {
                sender.sendMessage(langHandler.getMessage((Player) sender, "team.already-finalize"));
                return true;
            }
            for (List<Player> team: customTeams.values()) {
                if (team.contains(targetPlayer)) {
                    sender.sendMessage(langHandler.getMessage((Player) sender, "team.no-player"));
                    return true;
                }
            }
            customTeams.computeIfAbsent(teamName, k -> new ArrayList<>()).add(targetPlayer);
            sender.sendMessage(langHandler.getMessage((Player) sender, "team.added", targetPlayer.getName(), teamName));
        } else if (action.equalsIgnoreCase("remove")) {
            if (teamsFinalized) {
                sender.sendMessage(langHandler.getMessage((Player) sender, "team.already-finalize"));
                return true;
            }
            List<Player> team = customTeams.get(teamName);
            if (team != null && team.contains(targetPlayer)) {
                team.remove(targetPlayer);
                sender.sendMessage(langHandler.getMessage((Player) sender, "team.removed", targetPlayer.getName(), teamName));
            } else {
                sender.sendMessage(langHandler.getMessage((Player) sender, "team.no-removed", targetPlayer.getName(), teamName));
            }
        }

        return true;
    }
}
