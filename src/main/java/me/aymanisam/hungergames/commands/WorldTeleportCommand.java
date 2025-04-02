package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.aymanisam.hungergames.HungerGames.worldNames;

public class WorldTeleportCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final ArenaHandler arenaHandler;

    public WorldTeleportCommand(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = null;

        if (sender instanceof Player) {
            player = ((Player) sender);
        }

        if (player != null && !(sender.hasPermission("hungergames.teleport"))) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "no-permission"));
            return true;
        }

        List<String> allowedPlayers = new ArrayList<>(List.of("all"));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            allowedPlayers.add(onlinePlayer.getName());
        }

        if (args.length < 1) {
            sender.sendMessage(langHandler.getMessage(player, "teleport.no-player"));
            return true;
        }

        String playerToTeleport = args[0];

        if (!allowedPlayers.contains(playerToTeleport)) {
            sender.sendMessage(langHandler.getMessage(player, "teleport.invalid-player", playerToTeleport));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(langHandler.getMessage(player, "teleport.no-arena"));
            return true;
        }

        String worldName = args[1];

        if (!worldNames.contains(worldName)) {
            sender.sendMessage(langHandler.getMessage(player, "teleport.invalid-world", worldName));
            return false;
        }

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            world = Bukkit.createWorld(WorldCreator.name(worldName));
            assert world != null;
            arenaHandler.loadWorldFiles(world);
        }

        if (playerToTeleport.equalsIgnoreCase("all")) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.teleport(world.getSpawnLocation());
                onlinePlayer.sendMessage(langHandler.getMessage(onlinePlayer, "teleport.teleported", world.getName()));
            }

            sender.sendMessage(langHandler.getMessage(player, "teleport.sender", "Everyone" , world.getName()));
            return true;
        }

        Player p = Bukkit.getPlayer(playerToTeleport);
        assert p != null;
        p.teleport(world.getSpawnLocation());
        p.sendMessage(langHandler.getMessage(p, "teleport.teleported", world.getName()));

        return true;
    }
}
