package me.aymanisam.hungergames.handlers;

import org.bukkit.entity.Player;

public interface PacketAdapter {
	void setGlowing(Player playerToGlow, Player playerToSeeGlow, boolean glow);
	boolean isAvailable();
}
