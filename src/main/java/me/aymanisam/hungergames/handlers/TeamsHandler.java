package me.aymanisam.hungergames.handlers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import me.aymanisam.hungergames.HungerGames;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static me.aymanisam.hungergames.commands.ToggleChatCommand.playerChatModes;
import static me.aymanisam.hungergames.handlers.CountDownHandler.playersPerTeam;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class TeamsHandler {
    private final LangHandler langHandler;
    private final ScoreBoardHandler scoreBoardHandler;

    public static final List<List<Player>> teams = new ArrayList<>();
    public static final List<List<Player>> teamsAlive = new ArrayList<>();

    public TeamsHandler(HungerGames plugin, ScoreBoardHandler scoreBoardHandler) {
        this.langHandler = new LangHandler(plugin);
        this.scoreBoardHandler = scoreBoardHandler;
    }

    public void createTeam() {
        Collections.shuffle(playersAlive);
        teams.clear();
        teamsAlive.clear();

        boolean versus = false;

        int numTeams;
        if (playersPerTeam < 1) {
            numTeams = 2;
            versus = true;
        } else {
            numTeams = (playersAlive.size() + playersPerTeam - 1) / playersPerTeam;
        }

        for (int i = 0; i < numTeams; i++) {
            teams.add(new ArrayList<>());
        }

        for (int i = 0; i < playersAlive.size(); i++) {
            Player player = playersAlive.get(i);
            List<Player> team = teams.get(i % numTeams);
            team.add(player);
        }

        for (List<Player> team : teams) {
            List<Player> teamCopy = new ArrayList<>(team);
            teamsAlive.add(teamCopy);
            processTeam(team);
        }

        applyEffectsToPlayers();
    }

    private void applyHeartEffect(Player playerToEffect, Player playerToSeeEffect) {
        Particle<ParticleData> heartParticle = new Particle<>(ParticleTypes.HEART);
        EntityData heartEffect = new EntityData(17, EntityDataTypes.PARTICLE, heartParticle);
        List<EntityData> metadataList = Collections.singletonList(heartEffect);
        WrapperPlayServerEntityMetadata entityMetadataPacket = new WrapperPlayServerEntityMetadata(playerToEffect.getEntityId(), metadataList);
        PacketEvents.getAPI().getPlayerManager().sendPacket(playerToSeeEffect, entityMetadataPacket);
    }

    private void applyAngryEffect(Player playerToEffect, Player playerToSeeEffect) {
        Particle<ParticleData> angryParticle = new Particle<>(ParticleTypes.ANGRY_VILLAGER);
        EntityData heartEffect = new EntityData(17, EntityDataTypes.PARTICLE, angryParticle);
        List<EntityData> metadataList = Collections.singletonList(heartEffect);
        WrapperPlayServerEntityMetadata entityMetadataPacket = new WrapperPlayServerEntityMetadata(playerToEffect.getEntityId(), metadataList);
        PacketEvents.getAPI().getPlayerManager().sendPacket(playerToSeeEffect, entityMetadataPacket);
    }

    public void applyEffectsToPlayers() {
        for (List<Player> team : teams) {
            for (Player player : team) {
                // Apply heart effect to teammates
                for (Player teammate : team) {
                    if (!teammate.equals(player)) {
                        applyHeartEffect(teammate, player);
                    }
                }

                // Apply villager angry effect to enemies
                for (Player enemy : Bukkit.getOnlinePlayers()) {
                    if (!team.contains(enemy)) {
                        applyAngryEffect(enemy, player);
                    }
                }
            }
        }
    }

    private void processTeam(List<Player> team) {
        if (playersPerTeam != 1) {
            if (team.size() < playersPerTeam) {
                // Apply extra effects to players in teams with fewer players
                adjustPlayerHealthBasedOnTeamSize(team, playersPerTeam);
            }

            for (Player player : team) {
                sendTeamMessagesAndSetupItems(player, team);
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

    private void sendTeamMessagesAndSetupItems(Player player, List<Player> team) {
        langHandler.getLangConfig(player);
        int teamId = teams.indexOf(team) + 1;
        player.sendMessage(langHandler.getMessage("team.id", teamId));

        String teammateNames = getTeammateNames(team, player);
        if (!teammateNames.isEmpty()) {
            player.sendMessage(langHandler.getMessage("team.members", teammateNames));
            setupCompassForPlayer(player);
        } else {
            player.sendMessage(langHandler.getMessage("team.no-teammates"));
        }
    }

    private void setupCompassForPlayer(Player player) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(langHandler.getMessage("team.compass-teammate"));
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        List<String> lore = new ArrayList<>();
        lore.add(langHandler.getMessage("team.compass-click"));
        lore.add(langHandler.getMessage("team.compass-shift-click"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        player.getInventory().setItem(8, item);
    }

    public List<Player> getTeammates(Player currentPlayer) {
        for (List<Player> team : teams) {
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
        // Step 1: Create entity metadata for glowing effect
        byte glowingEffectValue;

        if (glow) {
            glowingEffectValue = (byte) 0x40; // Bit mask value for glowing effect
        } else {
            glowingEffectValue = (byte) 0x00; // Bit mask value for removing effect
        }

        EntityData metadata = new EntityData(0, EntityDataTypes.BYTE, glowingEffectValue);

        // Step 2: Create a list of EntityData and add the glowing effect metadata
        List<EntityData> metadataList = Collections.singletonList(metadata);

        // Step 3: Create the WrapperPlayServerEntityMetadata packet
        WrapperPlayServerEntityMetadata entityMetadataPacket = new WrapperPlayServerEntityMetadata(playerToGlow.getEntityId(), metadataList);

        // Step 4: Send the glowing effect packet to the player
        PacketEvents.getAPI().getPlayerManager().sendPacket(playerToSeeGlow, entityMetadataPacket);
    }

    public void removeGlowFromAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                playerGlow(player, viewer, false);
            }
        }
    }

    public boolean isPlayerInAnyTeam(Player player) {
        for (List<Player> team : teams) {
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
