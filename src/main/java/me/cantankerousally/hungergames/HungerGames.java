package me.cantankerousally.hungergames;

import me.cantankerousally.hungergames.commands.ArenaSelectorCommand;
import me.cantankerousally.hungergames.commands.ChestRefillCommand;
import me.cantankerousally.hungergames.handler.SetArenaHandler;
import org.bukkit.plugin.java.JavaPlugin;


public final class HungerGames extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("create").setExecutor(new ArenaSelectorCommand(this));
        getCommand("chestrefill").setExecutor(new ChestRefillCommand(this));
        getServer().getPluginManager().registerEvents(new SetArenaHandler(this), this);
    }
}
