package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ArenaHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;
import java.util.logging.Level;

import static me.aymanisam.hungergames.HungerGames.*;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class SignClickListener implements Listener {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ArenaHandler arenaHandler;

    private final Map<Player, Long> lastInteractTime = new HashMap<>();
    private final Map<Player, Long> lastMessageTime = new HashMap<>();

    public SignClickListener(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler, ArenaHandler arenaHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.arenaHandler = arenaHandler;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();

            Block block = event.getClickedBlock();
            assert block != null;

            long currentTime = System.currentTimeMillis();

            if (block.getState() instanceof Sign sign) {
                for (String worldName : hgWorldNames) {
                    if (sign.getSide(Side.FRONT).getLine(1).contains(worldName)) {
                        if (lastInteractTime.containsKey(player) && (currentTime - lastInteractTime.get(player)) < 5000) {
                            return; // Ignore the event if it's within the cooldown period
                        }

                        World world = Bukkit.getWorld(worldName);

                        if (lastMessageTime.containsKey(player) && (currentTime - lastMessageTime.get(player)) < 500) {
                            return; // Don't send another message if within cooldown
                        }

                        if (!worldCreated.getOrDefault(worldName, false)) {
                            player.sendMessage(langHandler.getMessage(player, "game.not-initialized"));
                            lastMessageTime.put(player, currentTime);
                            return;
                        }

                        if (gameStarting.getOrDefault(worldName, false)) {
                            player.sendMessage(langHandler.getMessage(player, "startgame.starting"));
                            lastMessageTime.put(player, currentTime);
                            return;
                        }

                        if (gameStarted.getOrDefault(worldName, false)) {
                            player.sendMessage(langHandler.getMessage(player, "startgame.started"));
                            lastMessageTime.put(player, currentTime);
                            return;
                        }

                        new AnvilGUI.Builder()
                                .onClick((slot, stateSnapshot) -> {
                                    if(slot != AnvilGUI.Slot.OUTPUT) {
                                        return Collections.emptyList();
                                    }

                                    String inputPin = stateSnapshot.getText();

                                    if (stringToInt(inputPin) != null && Integer.parseInt(inputPin) == (worldPins.get(worldName))) {
                                        teleportPlayer(worldName, world, player);
                                    } else {
                                        player.sendMessage((langHandler.getMessage(player, "arena.wrong-pin")));
                                    }
                                    return List.of(AnvilGUI.ResponseAction.close());
                                })
                                .text(langHandler.getMessage(player, "arena.pin-text"))
                                .title(langHandler.getMessage(player, "arena.pin-title"))
                                .plugin(plugin)
                                .open(player);

                        lastInteractTime.put(player, currentTime);
                        break;
                    }
                }
            }
        }
    }

//    @EventHandler
//    public void onInventoryClick(InventoryClickEvent event) {
//        System.out.println("Inventory Clicked");
//        if (event.getInventory().getType() == InventoryType.ANVIL) {
//            System.out.println("Anvil Inventory");
//            if (event.getRawSlot() == 2) { // Result slot
//                System.out.println("Result Slot");
//                ItemStack item = event.getCurrentItem();
//                System.out.println(item);
//                if (item != null && item.hasItemMeta()) {
//                    System.out.println("Item has meta");
//                    String inputPin = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
//                    Player player = (Player) event.getWhoClicked();
//                    String worldName = worldTeleportPlayer.get(player);
//                    World world = Bukkit.getWorld(worldName);
//
//                    if (stringToInt(inputPin) != null && Integer.parseInt(inputPin) == (worldPins.get(worldName))) {
//                        teleportPlayer(worldName, world, player);
//                    } else {
//                        player.sendMessage(langHandler.getMessage(player, "arena.wrong-pin"));
//                    }
//
//                    event.setCancelled(true);
//                    player.closeInventory();
//                }
//            }
//        }
//    }

    private Integer stringToInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void teleportPlayer(String worldName, World world, Player player) {
        if (world == null) {
            World createdWorld = Bukkit.createWorld(WorldCreator.name(worldName));
            assert createdWorld != null;
            arenaHandler.loadWorldFiles(createdWorld);
            if (setSpawnHandler.playersWaiting.get(createdWorld.getName()) != null && setSpawnHandler.playersWaiting.get(createdWorld.getName()).contains(player)) {
                return;
            }
            setSpawnHandler.teleportPlayerToSpawnpoint(player, createdWorld);
            setSpawnHandler.createSetSpawnConfig(createdWorld);
        } else {
            setSpawnHandler.teleportPlayerToSpawnpoint(player, world);
            setSpawnHandler.createSetSpawnConfig(world);
        }
    }

    public void setSignContent(List<Location> locations) {
        List<String> worlds = new ArrayList<>(hgWorldNames);
        Collections.sort(worlds);

        if (worlds.isEmpty() || locations.isEmpty()) {
            return;
        }

        for (Location location : locations) {
            String worldName;
            try {
                worldName = worlds.get(0);
            } catch (IndexOutOfBoundsException e) {
                plugin.getLogger().log(Level.WARNING, "Could not set sign content");
                return;
            }

            int worldPlayersWaitingSize = setSpawnHandler.playersWaiting.computeIfAbsent(worldName, k -> new ArrayList<>()).size();
            int worldSpawnPointSize = setSpawnHandler.spawnPoints.computeIfAbsent(worldName, k -> new ArrayList<>()).size();
            List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(worldName, k -> new ArrayList<>());

            if (location.getBlock().getState() instanceof Sign sign) {
                sign.setEditable(false);
                SignSide frontSide = sign.getSide(Side.FRONT);
                SignSide backSide = sign.getSide(Side.BACK);
                frontSide.setLine(0, ChatColor.BOLD + "Join");
                backSide.setLine(0, ChatColor.BOLD + "Join");
                frontSide.setLine(1, ChatColor.BOLD + worldName);
                backSide.setLine(1, ChatColor.BOLD + worldName);
                if (isGameStartingOrStarted(worldName)) {
                    frontSide.setLine(2, ChatColor.BOLD + "In Progress");
                    backSide.setLine(2, ChatColor.BOLD + "In Progress");
                    frontSide.setLine(3, ChatColor.BOLD + "" + worldPlayersAlive.size() + " Alive");
                    backSide.setLine(3, ChatColor.BOLD + "" + worldPlayersAlive.size() + " Alive");
                } else {
                    frontSide.setLine(2, ChatColor.BOLD + "Waiting");
                    backSide.setLine(2, ChatColor.BOLD + "Waiting");
                    frontSide.setLine(3, ChatColor.BOLD + "[" + worldPlayersWaitingSize + "/" + worldSpawnPointSize + "]");
                    backSide.setLine(3, ChatColor.BOLD + "[" + worldPlayersWaitingSize + "/" + worldSpawnPointSize + "]");
                }
                sign.update();
            }

            worlds.remove(0);
        }
    }
}
