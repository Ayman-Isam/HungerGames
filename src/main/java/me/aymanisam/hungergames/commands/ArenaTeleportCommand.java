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

public class ArenaTeleportCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final ArenaHandler arenaHandler;

    public ArenaTeleportCommand(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
        this.arenaHandler = new ArenaHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(sender.hasPermission("hungergames.teleport"))) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "no-permission"));
            return true;
        }

        List<String> allowedPlayers = new ArrayList<>(List.of("all"));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            allowedPlayers.add(onlinePlayer.getName());
        }

        if (args.length < 1) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "teleport.no-player"));
            return true;
        }

        String playerToTeleport = args[0];

        if (!allowedPlayers.contains(playerToTeleport)) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "teleport.invalid-player", playerToTeleport));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "teleport.no-arena"));
            return true;
        }

        String arenaName = args[1];

        if (!worldNames.contains(arenaName)) {
            sender.sendMessage(langHandler.getMessage((Player) sender, "teleport.invalid-arena", arenaName));
            return false;
        }

        World world = Bukkit.getWorld(arenaName);

        if (world == null) {
            world = Bukkit.createWorld(WorldCreator.name(arenaName));
            assert world != null;
            arenaHandler.loadWorldFiles(world);
        }

        if (playerToTeleport.equalsIgnoreCase("all")) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.teleport(world.getSpawnLocation());
                onlinePlayer.sendMessage(langHandler.getMessage(onlinePlayer, "teleport.teleported", world.getName()));
            }

            sender.sendMessage(langHandler.getMessage((Player) sender, "teleport.sender", "Everyone" , world.getName()));
            return true;
        }

        Player player = Bukkit.getPlayer(playerToTeleport);
        assert player != null;
        player.teleport(world.getSpawnLocation());
        player.sendMessage(langHandler.getMessage(player, "teleport.teleported", world.getName()));

        return true;
    }
}
