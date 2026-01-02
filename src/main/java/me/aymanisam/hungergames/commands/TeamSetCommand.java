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
	    Player player = null;

	    if (sender instanceof Player) {
		    player = ((Player) sender);
	    }

	    if (player != null && !player.hasPermission("hungergames.team")) {
		    player.sendMessage(langHandler.getMessage(player, "no-permission"));
		    return true;
	    }

	    if (args.length < 1) {
		    sender.sendMessage(langHandler.getMessage(player, "team.no-args"));
		    return true;
	    }

	    String action = args[0];

		List<String> validActions = List.of("add", "remove", "list", "finalize", "reset");

		if (!validActions.contains(action.toLowerCase())) {
			sender.sendMessage(langHandler.getMessage(player, "team.no-action"));
			return true;
        }

		if (action.equalsIgnoreCase("list")) {
			if (customTeams.isEmpty()) {
				sender.sendMessage(langHandler.getMessage(player, "team.no-list"));
				return true;
			}

            for (Map.Entry<String, List<Player>> entry : customTeams.entrySet()) {
                String team = entry.getKey();
                List<Player> members = entry.getValue();
                List<String> memberNames = members.stream().map(Player::getName).collect(Collectors.toList());
                sender.sendMessage(team + ": " + String.join(", ", memberNames));
            }

            return true;
        } else if (action.equalsIgnoreCase("reset")) {
            customTeams.clear();
            teamsFinalized = false;
            sender.sendMessage(langHandler.getMessage(player, "team.reset"));
            return true;
        } else if (action.equalsIgnoreCase("finalize")) {
            teamsFinalized = true;
            sender.sendMessage(langHandler.getMessage(player, "team.finalize"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(langHandler.getMessage(player, "team.no-args"));
            return true;
        }

        String teamName = args[1];
        String playerName = args[2];

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(langHandler.getMessage(player, "spectate.null-player"));
            return true;
        }

        if (action.equalsIgnoreCase("add")) {
            if (teamsFinalized) {
                sender.sendMessage(langHandler.getMessage(player, "team.already-finalize"));
                return true;
            }
            for (List<Player> team: customTeams.values()) {
                if (team.contains(targetPlayer)) {
                    sender.sendMessage(langHandler.getMessage(player, "team.no-player"));
                    return true;
                }
            }
            customTeams.computeIfAbsent(teamName, k -> new ArrayList<>()).add(targetPlayer);
            sender.sendMessage(langHandler.getMessage(player, "team.added", targetPlayer.getName(), teamName));
        } else if (action.equalsIgnoreCase("remove")) {
            if (teamsFinalized) {
                sender.sendMessage(langHandler.getMessage(player, "team.already-finalize"));
                return true;
            }
            List<Player> team = customTeams.get(teamName);
            if (team != null && team.contains(targetPlayer)) {
                team.remove(targetPlayer);
                sender.sendMessage(langHandler.getMessage(player, "team.removed", targetPlayer.getName(), teamName));
            } else {
                sender.sendMessage(langHandler.getMessage(player, "team.no-removed", targetPlayer.getName(), teamName));
            }
        }

        return true;
    }
}
