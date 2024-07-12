package me.aymanisam.hungergames.handlers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static com.github.retrooper.packetevents.protocol.potion.PotionTypes.GLOWING;
import static me.aymanisam.hungergames.HungerGames.gameWorld;
import static me.aymanisam.hungergames.commands.ToggleChatCommand.playerChatModes;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.playersAlive;

public class TeamsHandler {
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;

    public static final List<List<Player>> teams = new ArrayList<>();
    public static final List<List<Player>> teamsAlive = new ArrayList<>();

    public TeamsHandler(HungerGames plugin) {
        this.langHandler = new LangHandler(plugin);
        this.configHandler = new ConfigHandler(plugin);
    }

    public void createTeam() {
        int teamSizeConfig = configHandler.getWorldConfig(gameWorld).getInt("players-per-team");
        if (teamSizeConfig > 1) {
            int teamSize = configHandler.getWorldConfig(gameWorld).getInt("players-per-team");
            int numTeams = (playersAlive.size() + teamSize - 1) / teamSize;
            Collections.shuffle(playersAlive);
            teams.clear();


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
            }

            for (int i = 0; i < numTeams; i++) {
                List<Player> team = teams.get(i);

                makeTeammatesGlow(team);

                if (team.size() < teamSizeConfig) {
                    // Apply extra effects to players in teams with fewer players
                    double ratio = teamSizeConfig / (double) team.size();
                    double newMaxHealth = 20.0 * ratio;
                    int newMaxHealthRounded = (int) Math.round(newMaxHealth);
                    for (Player player : team) {
                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealthRounded);
                        player.setHealth(newMaxHealthRounded);
                    }
                }

                for (Player player : team) {
                    langHandler.getLangConfig(player);
                    player.sendMessage(langHandler.getMessage("team.id", (i + 1)));

                    if (!getTeammateNames(team, player).isEmpty()) {
                        player.sendMessage(langHandler.getMessage("team.members",getTeammateNames(team, player)));
                        ItemStack item = new ItemStack(Material.COMPASS);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(langHandler.getMessage("team.compass-teammate"));
                        meta.addEnchant(Enchantment.DURABILITY, 1, true);
                        List<String> lore = new ArrayList<>();
                        lore.add(langHandler.getMessage("team.compass-click"));
                        lore.add(langHandler.getMessage("team.compass-shift-click"));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        player.getInventory().setItem(8, item);
                    } else {
                        player.sendMessage(langHandler.getMessage("team.no-teammates"));
                    }
                }
            }
        }
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

    private void makeTeammatesGlow(List<Player> team) {
        for (Player playerToGlow: team) {
            for (Player playerToSeeGlow : team) {
                if (!playerToGlow.equals(playerToSeeGlow)) {
                    makePlayerGlow(playerToGlow, playerToSeeGlow);
                }
            }
        }
    }

    public void makePlayerGlow(Player playerToGlow, Player playerToSeeGlow) {
        int entityId = playerToGlow.getEntityId();
        WrapperPlayServerEntityEffect entityEffectPacket = new WrapperPlayServerEntityEffect(entityId, GLOWING, 0, 1000, (byte)0);
        System.out.println("Sending glow packet to " + playerToSeeGlow.getName() + " for " + playerToGlow.getName());
        PacketEvents.getAPI().getPlayerManager().sendPacket(playerToSeeGlow, entityEffectPacket);
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
        return playerChatModes.getOrDefault(player, true);
    }
}
