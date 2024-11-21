package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import me.aymanisam.hungergames.handlers.SignHandler;
import me.aymanisam.hungergames.listeners.SignClickListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static me.aymanisam.hungergames.HungerGames.worldNames;

public class SignSetCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final List<Location> signLocations = new ArrayList<>();
    private final SignClickListener signClickListener;
    private final SignHandler signHandler;

    public SignSetCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, ArenaHandler arenaHandler) {
        this.langHandler = langHandler;
        this.signClickListener = new SignClickListener(langHandler, setSpawnHandler, arenaHandler);
        this.signHandler = new SignHandler(plugin);
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

        Block targetBlock = player.getTargetBlockExact(10);
        Location targetBlockLocation = null;
        if (targetBlock != null) {
            targetBlockLocation = targetBlock.getLocation();
        }

        if (targetBlockLocation == null || !(targetBlock.getState() instanceof Sign)) {
            sender.sendMessage(langHandler.getMessage(player, "game.invalid-target"));
            return true;
        }

        if (checkNearbySigns(player, targetBlockLocation)) {
            return true;
        }

        signHandler.saveSignLocations(signLocations);

        player.sendMessage(langHandler.getMessage(player, "game.sign-set"));

        signClickListener.setSignContent(signHandler.loadSignLocations());

        return true;
    }

    private boolean checkNearbySigns(Player player, Location targetBlockLocation) {
        int numSigns = worldNames.size() - 2; // 1 lobbyworld and 1 because it's the target block

        if (numSigns < 0) {
            player.sendMessage(langHandler.getMessage(player, "game.no-worlds", numSigns));
            signHandler.saveSignLocations(Collections.emptyList());
            signLocations.clear();
            return true;
        }

        Location finalBlockLocation = targetBlockLocation.clone();

        switch (player.getFacing()) {
            case NORTH -> finalBlockLocation.add(numSigns, 0, 0);
            case SOUTH -> finalBlockLocation.add(-numSigns, 0, 0);
            case EAST -> finalBlockLocation.add(0, 0, numSigns);
            case WEST -> finalBlockLocation.add(0, 0, -numSigns);
            default -> Bukkit.getLogger().log(Level.SEVERE, "WRONG DIRECTION");
        }

        int startX = targetBlockLocation.getBlockX();
        int endX = finalBlockLocation.getBlockX();
        int startZ = targetBlockLocation.getBlockZ();
        int endZ = finalBlockLocation.getBlockZ();

        for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); x++) {
            for (int z = Math.min(startZ, endZ); z <= Math.max(startZ, endZ); z++) {
                Location currentLocation = new Location(targetBlockLocation.getWorld(), x, targetBlockLocation.getBlockY(), z);
                Block currentBlock = currentLocation.getBlock();
                if (!(currentBlock.getState() instanceof Sign)) {
                    player.sendMessage(langHandler.getMessage(player, "game.invalid-nearby", numSigns));
                    signLocations.clear();
                    return true;
                }
                signLocations.add(currentLocation);
            }
        }
        return false;
    }
}