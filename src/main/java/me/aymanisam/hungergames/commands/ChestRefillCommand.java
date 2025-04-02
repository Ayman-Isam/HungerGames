package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.ChestRefillHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.hgWorldNames;

public class ChestRefillCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ArenaHandler arenaHandler;
    private final ChestRefillHandler chestRefillHandler;

    public ChestRefillCommand(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
        this.chestRefillHandler = new ChestRefillHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player player = null;

        if (sender instanceof Player) {
            player = ((Player) sender);
        }

        if (player != null && !player.hasPermission("hungergames.chestrefill")) {
            player.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        FileConfiguration ArenaConfig;

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
            ArenaConfig = arenaHandler.getArenaConfig(plugin.getServer().getWorld(worldName));
        } else {
            ArenaConfig = arenaHandler.getArenaConfig(player.getWorld());
        }

        String worldName = ArenaConfig.getString("region.world");

        if (worldName == null) {
            sender.sendMessage(langHandler.getMessage(player, "chestrefill.no-arena"));
            return true;
        }

        chestRefillHandler.refillChests(plugin.getServer().getWorld(worldName));

        return true;
    }
}
