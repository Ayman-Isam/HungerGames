package me.aymanisam.hungergames.handlers;

import me.aymanisam.hungergames.HungerGames;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.aymanisam.hungergames.HungerGames.gameWorld;
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
                    player.sendMessage(langHandler.getMessage("team.id") + (i + 1));

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
}
