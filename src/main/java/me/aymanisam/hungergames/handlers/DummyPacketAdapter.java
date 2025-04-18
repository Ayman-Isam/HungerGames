package me.aymanisam.hungergames.handlers;

import org.bukkit.entity.Player;

public class DummyPacketAdapter implements PacketAdapter {
	@Override
	public void setGlowing(Player playerToGlow, Player playerToSeeGlow, boolean glow) {

	}

	@Override
	public boolean isAvailable() {
		return false;
	}
}
