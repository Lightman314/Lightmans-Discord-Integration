package io.github.lightman314.lightmansconsole.discord.links;

import java.util.UUID;

import io.github.lightman314.lightmansconsole.util.PlayerUtil;
import net.minecraft.entity.player.PlayerEntity;

public class LinkedAccount {

	public final UUID playerID;
	public String getName() { return PlayerUtil.playerName(this.playerID); }
	public final String discordID;

	public LinkedAccount(UUID id, String discordID) {
		this.playerID = id;
		this.discordID = discordID;
	}

	public boolean equalsPlayer(PlayerEntity player) {
		return player.getUniqueID().equals(this.playerID);
	}
	
	public boolean equalsPlayerName(String playerName)
	{
		return this.getName().equals(playerName);
	}

	public boolean equalsDiscordID(String discordID) {
		return this.discordID.equals(discordID);
	}

}
