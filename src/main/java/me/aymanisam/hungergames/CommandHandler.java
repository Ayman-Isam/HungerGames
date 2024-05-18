package me.aymanisam.hungergames;

import me.aymanisam.hungergames.commands.*;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final HungerGames plugin;
    private final LangHandler langHandler;

    public CommandHandler(HungerGames plugin) {
        this.plugin = plugin;
        this.langHandler = new LangHandler(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            CommandExecutor executor;
            switch (args[0].toLowerCase()) {
                case "join":
                    executor = new JoinGameCommand(plugin);
                    break;
                case "start":
                    executor = new StartGameCommand(plugin);
                    break;
                case "spectate":
                    executor = new SpectatePlayerCommand(plugin);
                    break;
                case "select":
                    executor = new ArenaSelectCommand(plugin);
                    break;
                case "end":
                    executor = new EndGameCommand(plugin);
                    break;
                case "chestrefill":
                    executor = new ChestRefillCommand(plugin);
                    break;
                case "supplydrop":
                    executor = new SupplyDropCommand(plugin);
                    break;
                case "setspawn":
                    executor = new SetSpawnCommand(plugin);
                    break;
                case "create":
                    executor = new ArenaCreateCommand(plugin);
                    break;
                case "scanarena":
                    executor = new ArenaScanCommand(plugin);
                    break;
                case "border":
                    executor = new BorderSetCommand(plugin);
                    break;
                case "reloadconfig":
                    executor = new ReloadConfigCommand(plugin);
                    break;
                default:
                    if (sender instanceof Player) {
                        langHandler.loadLanguageConfig((Player) sender);
                    }
                    sender.sendMessage(langHandler.getMessage("unknown-subcommand") + args[0]);
                    return false;
            }
            return executor.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        } else {
            sender.sendMessage(langHandler.getMessage("usage"));
            return false;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String[] commands = {"join", "start", "spectate", "select", "end", "chestrefill", "supplydrop", "setspawn", "create", "scanarena", "border", "reloadconfig"};
            for (String subcommand : commands) {
                if (sender.hasPermission("hungergames." + subcommand)) {
                    completions.add(subcommand);
                }
            }
            return completions;
        } else if (args[0].equalsIgnoreCase("border")) {
            List<String> completions = new ArrayList<>();
            switch (args.length) {
                case 2:
                    completions.add(langHandler.getMessage("border.args-1"));
                case 3:
                    completions.add(langHandler.getMessage("border.args-2"));
                case 4:
                    completions.add(langHandler.getMessage("border.args-3"));
            return completions;
            }
        }
        return new ArrayList<>();
    }
}
