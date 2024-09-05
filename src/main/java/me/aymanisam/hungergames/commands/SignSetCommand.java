package me.aymanisam.hungergames.commands;

import com.google.common.collect.Sets;
import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import me.aymanisam.hungergames.listeners.SignClickListener;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.aymanisam.hungergames.HungerGames.worldNames;

public class SignSetCommand implements CommandExecutor {
    private final LangHandler langHandler;
    private final List<Location> signLocations = new ArrayList<>();
    private final SignClickListener signClickListener;
    private final SetSpawnHandler setSpawnHandler;

    public SignSetCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler) {
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.signClickListener = new SignClickListener(plugin, langHandler, setSpawnHandler);
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
        assert targetBlock != null;
        Location targetBlockLocation = targetBlock.getLocation();

        if (!(targetBlock.getState() instanceof Sign)) {
            sender.sendMessage(langHandler.getMessage(player, "game.invalid-target"));
            return true;
        }

        int numSigns = worldNames.size() - 2;

        Location finalBlockLocation = targetBlockLocation.clone().add(-numSigns,0,0);

        int startX = targetBlockLocation.getBlockX();
        int endX = finalBlockLocation.getBlockX();

        for (int x = startX; x >= endX; x--) {
            Location currentLocation = targetBlockLocation.clone();
            currentLocation.setX(x);
            Block currentBlock = currentLocation.getBlock();
            if (!(currentBlock.getState() instanceof Sign)) {
                player.sendMessage(langHandler.getMessage(player, "game.invalid-nearby", numSigns));
                signLocations.clear();
                return true;
            }
            signLocations.add(currentLocation);
        }

        signClickListener.setSignContent(signLocations);

        return true;
    }
}