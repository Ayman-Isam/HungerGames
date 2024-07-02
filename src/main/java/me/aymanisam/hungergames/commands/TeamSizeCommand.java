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

    public TeamSizeCommand(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
        this.configHandler = new ConfigHandler(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage("no-server"));
            return true;
        }

        langHandler.getLangConfig(player);

        if (!(player.hasPermission("hungergames.teamsize"))) {
            sender.sendMessage(langHandler.getMessage("no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(langHandler.getMessage("team.usage"));
            return true;
        }

        int newSize = Integer.parseInt(args[0]);

        World world = player.getWorld();

        configHandler.createWorldConfig(world);
        configHandler.getWorldConfig(world).set("players-per-team", newSize);
        configHandler.saveWorldConfig(world);

        System.out.println(configHandler);

        sender.sendMessage(langHandler.getMessage("team.size", newSize));

        return true;
    }
}
