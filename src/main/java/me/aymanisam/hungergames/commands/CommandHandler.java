package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handler.SetSpawnHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final HungerGames plugin;

    public CommandHandler(HungerGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            CommandExecutor executor;
            switch (args[0].toLowerCase()) {
                case "supplydrop":
                    executor = new SupplyDropCommand(plugin);
                    break;
                case "create":
                    executor = new ArenaCreateCommand(plugin);
                    break;
                case "select":
                    executor = new ArenaSelectCommand(plugin);
                    break;
                case "setspawn":
                    executor = new SetSpawnCommand(plugin);
                    break;
                case "join":
                    SetSpawnHandler setSpawnHandler = plugin.getSetSpawnHandler();
                    executor = new JoinGameCommand(plugin, setSpawnHandler);
                    break;
                case "spectate":
                    executor = new SpectateCommand(plugin, plugin.getGameHandler());
                    break;
                case "chestrefill":
                    executor = new ChestRefillCommand(plugin);
                    break;
                case "start":
                    executor = new StartGameCommand(plugin);
                    break;
                case "end":
                    executor = new EndGameCommand(plugin);
                    break;
                case "scanarena":
                    executor = new ScanArenaCommand(plugin);
                    break;
                case "border":
                    executor = new BorderSetCommand(plugin);
                    break;
                case "reloadconfig":
                    executor = new ReloadConfigCommand(plugin);
                    break;
                default:
                    sender.sendMessage("Unknown sub-command: " + args[0]);
                    return false;
            }
            return executor.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        } else {
            sender.sendMessage("Usage: /hg <subcommand> [args]");
            return false;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String[] commands = {"join", "spectate","start", "end","supplydrop", "create", "select", "setspawn", "chestrefill", "scanarena", "border", "reloadconfig"};
            for (String cmd : commands) {
                if (sender.hasPermission("hungergames." + cmd)) {
                    completions.add(cmd);
                }
            }
            return completions;
        } else if (args[0].equalsIgnoreCase("border")) {
            List<String> completions = new ArrayList<>();
            if (args.length == 2) {
                completions.add(plugin.getMessage("border.args-1"));
            } else if (args.length == 3) {
                completions.add(plugin.getMessage("border.args-2"));
            } else if (args.length == 4) {
                completions.add(plugin.getMessage("border.args-3"));
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
