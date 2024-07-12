package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.*;
import static me.aymanisam.hungergames.listeners.TeamVotingListener.giveVotingBook;

public class MapChangeCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;

    public MapChangeCommand(HungerGames plugin, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.setSpawnHandler = setSpawnHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
            return true;
        }

        langHandler.getLangConfig(player);

        if (!player.hasPermission("hungergames.map")) {
            player.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        if (gameStarted || gameStarting) {
            player.sendMessage(langHandler.getMessage("map.game-running"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(langHandler.getMessage("map.no-args"));
            return false;
        }

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (setSpawnHandler.spawnPointMap.containsValue(p)) {
                setSpawnHandler.removePlayerFromSpawnPoint(p);
            }
        }

        String mapName = args[0];
        World world = plugin.getServer().getWorld(mapName);

        if (world == null) {
            sender.sendMessage(langHandler.getMessage("map.not-found", mapName));
            plugin.getLogger().info("Loaded maps:" + plugin.getServer().getWorlds().stream().map(World::getName).collect(Collectors.joining(", ")));
            return false;
        }

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.teleport(world.getSpawnLocation());
            setSpawnHandler.teleportPlayerToSpawnpoint(p);
        }

        sender.sendMessage(langHandler.getMessage("map.switched", mapName));

        giveVotingBook(player, langHandler);

        gameWorld = world;

        return true;
    }
}
