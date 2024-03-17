package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.handler.SetSpawnHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinGameCommand implements CommandExecutor {
    private final SetSpawnHandler setSpawnHandler;

    public JoinGameCommand(SetSpawnHandler setSpawnHandler) {
        this.setSpawnHandler = setSpawnHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        setSpawnHandler.handleJoin(player);
        return true;
    }
}
