package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import me.aymanisam.hungergames.handlers.SignHandler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.aymanisam.hungergames.HungerGames.hgWorldNames;
import static me.aymanisam.hungergames.handlers.SignHandler.signLocations;

public class SignSetCommand implements CommandExecutor {
	private final HungerGames plugin;
    private final LangHandler langHandler;
	private final SignHandler signHandler;
    public static final Map<String, String> slots = new HashMap<>();

	public SignSetCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler) {
		this.plugin = plugin;
        this.langHandler = langHandler;
		this.signHandler = new SignHandler(plugin, setSpawnHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(player.hasPermission("hungergames.setsign"))) {
            sender.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

		if (args.length < 1) {
			sender.sendMessage(langHandler.getMessage(player, "game.no-action"));
			return true;
		}

		String action = args[0];

	    List<String> validActions = List.of("create", "remove", "assign", "list");

	    if (!validActions.contains(action.toLowerCase())) {
		    sender.sendMessage(langHandler.getMessage(player, "game.no-args"));
		    return true;
	    }

		if (action.equalsIgnoreCase("list")) {
			if (slots.isEmpty()) {
				sender.sendMessage(langHandler.getMessage(player, "game.no-list"));
				return true;
			}

			for (Map.Entry<String, String> entry : slots.entrySet()) {
				sender.sendMessage(entry.getKey() + " - " + entry.getValue());
			}

			return true;
		}

	    if (args.length < 2) {
		    sender.sendMessage(langHandler.getMessage(player, "game.no-args"));
		    return true;
	    }

	    String slotName = args[1];

	    if (action.equalsIgnoreCase("remove")) {
			if (!slots.containsKey(slotName)) {
				sender.sendMessage(langHandler.getMessage(player, "game.no-slot"), slotName);
				return true;
			}

			slots.remove(slotName);
			signLocations.remove(slotName);
			sender.sendMessage(langHandler.getMessage(player, "game.removed-slot", slotName));
			plugin.getConfigHandler().setSlots();
			signHandler.setSignContent();
		} else if (action.equalsIgnoreCase("assign")) {
			if (!slots.containsKey(slotName)) {
				sender.sendMessage(langHandler.getMessage(player, "game.no-slot", slotName));
				return true;
			}

			Block targetBlock = player.getTargetBlockExact(10);
			Location targetBlockLocation = null;
			if (targetBlock != null) {
				targetBlockLocation = targetBlock.getLocation();
			}

			if (targetBlockLocation == null || !(targetBlock.getState() instanceof Sign)) {
				sender.sendMessage(langHandler.getMessage(player, "game.invalid-target"));
				return true;
			}

			signLocations.put(slotName, targetBlockLocation);
			plugin.getConfigHandler().saveSignLocations();
			signHandler.setSignContent();
			sender.sendMessage(langHandler.getMessage(player, "game.assigned-slot", slotName));
		} else if (action.equalsIgnoreCase("create")) {
			if (args.length < 3) {
				sender.sendMessage(langHandler.getMessage(player, "game.no-args"));
				return true;
			}

			if (slots.containsKey(slotName)) {
				sender.sendMessage(langHandler.getMessage(player, "game.slot-exists", slotName));
				return true;
			}

			String worldName = args[2];

			if (!hgWorldNames.contains(worldName)) {
				sender.sendMessage(langHandler.getMessage(player, "teleport.invalid-world", worldName));
				plugin.getLogger().info("Loaded maps :" + String.join(", ", hgWorldNames));
				return true;
			}

			slots.put(slotName, worldName);
			plugin.getConfigHandler().setSlots();
			sender.sendMessage(langHandler.getMessage(player, "game.created-slot", slotName, worldName));
		}

		return true;
    }

}