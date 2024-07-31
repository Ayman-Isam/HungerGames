package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamSizeCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;

    public TeamSizeCommand(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        ;

        if (!(player.hasPermission("hungergames.teamsize"))) {
            sender.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(langHandler.getMessage(player, "team.usage"));
            return true;
        }

        int newSize = Integer.parseInt(args[0]);

        World world = player.getWorld();

        configHandler.createWorldConfig(world);
        configHandler.getWorldConfig(world).set("players-per-team", newSize);
        configHandler.saveWorldConfig(world);


        sender.sendMessage(langHandler.getMessage(player, "team.size", newSize));

        return true;
    }
}
