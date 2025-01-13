package me.aymanisam.hungergames;

import me.aymanisam.hungergames.commands.*;
import me.aymanisam.hungergames.handlers.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.aymanisam.hungergames.HungerGames.hgWorldNames;
import static me.aymanisam.hungergames.HungerGames.worldNames;

public class CommandDispatcher implements CommandExecutor, TabCompleter {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final GameSequenceHandler gameSequenceHandler;
    private final TeamsHandler teamsHandler;
    private final ScoreBoardHandler scoreBoardHandler;
    private final CountDownHandler countDownHandler;
    private final ArenaHandler arenaHandler;

    public CommandDispatcher(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, GameSequenceHandler gameSequenceHandler, TeamsHandler teamsHandler, ScoreBoardHandler scoreBoardHandler, CountDownHandler countDownHandler, ArenaHandler arenaHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.gameSequenceHandler = gameSequenceHandler;
        this.teamsHandler = teamsHandler;
        this.scoreBoardHandler = scoreBoardHandler;
        this.countDownHandler = countDownHandler;
        this.arenaHandler = arenaHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            CommandExecutor executor;
            switch (args[0].toLowerCase()) {
                case "join":
                    executor = new JoinGameCommand(plugin, langHandler, setSpawnHandler);
                    break;
                case "lobby":
                    executor = new LobbyReturnCommand(plugin, langHandler, setSpawnHandler, arenaHandler, countDownHandler, scoreBoardHandler);
                    break;
                case "start":
                    executor = new StartGameCommand(plugin, langHandler, setSpawnHandler, countDownHandler);
                    break;
                case "teamchat":
                    executor = new ToggleChatCommand(plugin, langHandler, teamsHandler);
                    break;
                case "spectate":
                    executor = new SpectatePlayerCommand(langHandler);
                    break;
                case "select":
                    executor = new ArenaSelectCommand(langHandler);
                    break;
                case "end":
                    executor = new EndGameCommand(plugin, langHandler, gameSequenceHandler, countDownHandler, setSpawnHandler);
                    break;
                case "initialize":
                    executor = new ArenaInitializeCommand(plugin, langHandler);
                    break;
                case "teleport":
                    executor = new WorldTeleportCommand(plugin, langHandler);
                    break;
                case "chestrefill":
                    executor = new ChestRefillCommand(plugin, langHandler);
                    break;
                case "supplydrop":
                    executor = new SupplyDropCommand(plugin, langHandler);
                    break;
                case "setspawn":
                    executor = new SetSpawnCommand(langHandler, setSpawnHandler);
                    break;
                case "create":
                    executor = new ArenaCreateCommand(plugin, langHandler);
                    break;
                case "scanarena":
                    executor = new ArenaScanCommand(plugin, langHandler);
                    break;
                case "border":
                    executor = new BorderSetCommand(plugin, langHandler);
                    break;
                case "reloadconfig":
                    executor = new ReloadConfigCommand(plugin, langHandler);
                    break;
                case "saveworld":
                    executor = new SaveWorldCommand(plugin, langHandler);
                    break;
                case "setsign":
                    executor = new SignSetCommand(plugin, langHandler, setSpawnHandler, arenaHandler);
                    break;
                default:
                    sender.sendMessage(langHandler.getMessage(sender instanceof Player ? (Player) sender : null, "unknown-subcommand", args[0]));
                    return false;
            }
            return executor.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        } else {
            sender.sendMessage(langHandler.getMessage(sender instanceof Player ? (Player) sender : null, "usage"));
            return false;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                String[] commands = {"join", "lobby", "start", "spectate", "select", "end", "teamchat", "teleport", "modifiers", "saveworld", "teamsize", "chestrefill", "supplydrop", "setspawn", "create", "scanarena", "border", "reloadconfig", "setsign"};

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
                        completions.add(langHandler.getMessage(player, "border.args-1"));
                        break;
                    case 3:
                        completions.add(langHandler.getMessage(player, "border.args-2"));
                        break;
                    case 4:
                        completions.add(langHandler.getMessage(player, "border.args-3"));
                        break;
                }

                return completions;
            } else if (args[0].equalsIgnoreCase("join")) {
                if (args.length == 2) {
                    return hgWorldNames;
                }
            } else if (args[0].equalsIgnoreCase("teleport")) {
                if (args.length == 2) {
                    List<String> allowedPlayers = new ArrayList<>(List.of("all"));

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        allowedPlayers.add(onlinePlayer.getName());
                    }

                    return allowedPlayers;
                }
                else if (args.length == 3) {
                    return worldNames;
                }
            }
        }
        return new ArrayList<>();
    }
}
