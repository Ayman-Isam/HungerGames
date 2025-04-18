package me.aymanisam.hungergames.handlers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PacketEventsAdapter implements PacketAdapter {
	@Override
	public void setGlowing(Player playerToGlow, Player playerToSeeGlow, boolean glow) {
		byte glowingEffectValue = glow ? (byte) 0x40 : (byte) 0x00;

		EntityData metadata = new EntityData(0, EntityDataTypes.BYTE, glowingEffectValue);
		List<EntityData> metadataList = Collections.singletonList(metadata);

		WrapperPlayServerEntityMetadata entityMetadataPacket = new WrapperPlayServerEntityMetadata(playerToGlow.getEntityId(), metadataList);

		PacketEvents.getAPI().getPlayerManager().sendPacket(playerToSeeGlow, entityMetadataPacket);
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
}
