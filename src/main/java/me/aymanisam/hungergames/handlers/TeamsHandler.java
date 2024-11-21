package me.aymanisam.hungergames.handlers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static me.aymanisam.hungergames.commands.ToggleChatCommand.playerChatModes;
import static me.aymanisam.hungergames.handlers.CountDownHandler.playersPerTeam;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class TeamsHandler {
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;

    public static final Map<String, List<List<Player>>> teams = new HashMap<>();
    public static final Map<String, List<List<Player>>> teamsAlive = new HashMap<>();

    public TeamsHandler(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
        this.configHandler = plugin.getConfigHandler();
    }

    public void createTeam(World world) {
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        Collections.shuffle(worldPlayersAlive);

        List<List<Player>> worldTeams = teams.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        List<List<Player>> worldTeamsAlive = teamsAlive.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        worldTeams.clear();
        worldTeamsAlive.clear();

        int numTeams;
        if (playersPerTeam < 1) {
            numTeams = 2;
        } else {
            numTeams = (worldPlayersAlive.size() + playersPerTeam - 1) / playersPerTeam;
        }

        for (int i = 0; i < numTeams; i++) {
            worldTeams.add(new ArrayList<>());
        }

        for (int i = 0; i < worldPlayersAlive.size(); i++) {
            Player player = worldPlayersAlive.get(i);
            List<Player> team = worldTeams.get(i % numTeams);
            team.add(player);
        }

        for (List<Player> team : worldTeams) {
            List<Player> teamCopy = new ArrayList<>(team);
            worldTeamsAlive.add(teamCopy);
            processTeam(team, world);
        }
    }

    private void processTeam(List<Player> team, World world) {
        if (playersPerTeam != 1) {
            if (team.size() < playersPerTeam) {
                // Apply extra effects to players in teams with fewer players
                adjustPlayerHealthBasedOnTeamSize(team, playersPerTeam);
            }

            for (Player player : team) {
                sendTeamMessagesAndSetupItems(player, team, world);
            }
        }
    }

    private void adjustPlayerHealthBasedOnTeamSize(List<Player> team, int teamSizeConfig) {
        double ratio = teamSizeConfig / (double) team.size();
        double newMaxHealth = 20.0 * ratio;
        int newMaxHealthRounded = (int) Math.round(newMaxHealth);
        for (Player player : team) {
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(newMaxHealthRounded);
            player.setHealth(newMaxHealthRounded);
        }
    }

    private void sendTeamMessagesAndSetupItems(Player player, List<Player> team, World world) {
        List<List<Player>> worldTeams = teams.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        int teamId = worldTeams.indexOf(team) + 1;
        player.sendMessage(langHandler.getMessage(player, "team.id", teamId));

        String teammateNames = getTeammateNames(team, player);
        if (!teammateNames.isEmpty()) {
            player.sendMessage(langHandler.getMessage(player, "team.members", teammateNames));
            setupCompassForPlayer(player);
        } else {
            player.sendMessage(langHandler.getMessage(player, "team.no-teammates"));
        }
    }

    private void setupCompassForPlayer(Player player) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(langHandler.getMessage(player, "team.compass-teammate"));
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        List<String> lore = new ArrayList<>();
        lore.add(langHandler.getMessage(player, "team.compass-click"));
        lore.add(langHandler.getMessage(player, "team.compass-shift-click"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        player.getInventory().setItem(8, item);
    }

    public List<Player> getTeammates(Player currentPlayer, World world) {
        List<List<Player>> worldTeams = teams.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        for (List<Player> team : worldTeams) {
            if (team.contains(currentPlayer)) {
                List<Player> teammates = new ArrayList<>(team);
                teammates.remove(currentPlayer);
                return teammates;
            }
        }
        return Collections.emptyList();
    }

    private String getTeammateNames(List<Player> team, Player currentPlayer) {
        StringBuilder teammates = new StringBuilder();
        for (Player player : team) {
            if (player != currentPlayer) {
                if (!teammates.isEmpty()) {
                    teammates.append(", ");
                }
                teammates.append(player.getName());
            }
        }
        return teammates.toString();
    }

    public void playerGlow(Player playerToGlow, Player playerToSeeGlow, Boolean glow) {
        if (!configHandler.getWorldConfig(playerToGlow.getWorld()).getBoolean("packetevents")) {
            return;
        }

        byte glowingEffectValue;

        if (glow) {
            glowingEffectValue = (byte) 0x40; // Bit mask value for glowing effect
        } else {
            glowingEffectValue = (byte) 0x00; // Bit mask value for removing effect
        }

        EntityData metadata = new EntityData(0, EntityDataTypes.BYTE, glowingEffectValue);

        List<EntityData> metadataList = Collections.singletonList(metadata);

        WrapperPlayServerEntityMetadata entityMetadataPacket = new WrapperPlayServerEntityMetadata(playerToGlow.getEntityId(), metadataList);

        PacketEvents.getAPI().getPlayerManager().sendPacket(playerToSeeGlow, entityMetadataPacket);
    }

    public void removeGlowFromAllPlayers(World world) {
        for (Player player : world.getPlayers()) {
            for (Player viewer : world.getPlayers()) {
                playerGlow(player, viewer, false);
            }
        }
    }

    public boolean isPlayerInAnyTeam(Player player, World world) {
        List<List<Player>> worldTeams = teams.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        for (List<Player> team : worldTeams) {
            if (team.contains(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isChatModeEnabled(Player player) {
        return playerChatModes.getOrDefault(player, false);
    }
}
