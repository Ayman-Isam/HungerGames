package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SupplyDropHandler;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.hgWorldNames;

public class SupplyDropCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SupplyDropHandler supplyDropHandler;

    public SupplyDropCommand(HungerGames plugin, LangHandler langHandler) {
	    this.plugin = plugin;
	    this.langHandler = langHandler;
        this.supplyDropHandler = new SupplyDropHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player player = null;

        if (sender instanceof Player) {
            player = ((Player) sender);
        }

        if (player != null && !player.hasPermission("hungergames.supplydrop")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        World world;

        if (player == null) {
            if (args.length != 1) {
                sender.sendMessage(langHandler.getMessage(null, "no-world"));
                return true;
            }
            String worldName = args[0];
            if (!hgWorldNames.contains(worldName)) {
                sender.sendMessage(langHandler.getMessage(null, "teleport.invalid-world", args[0]));
                plugin.getLogger().info("Loaded maps:" + plugin.getServer().getWorlds().stream().map(World::getName).collect(Collectors.joining(", ")));
                return true;
            }
            world = plugin.getServer().getWorld(worldName);
        } else {
            world = player.getWorld();
        }

        assert world != null;

        supplyDropHandler.setSupplyDrop(world);

        return true;
    }
}
