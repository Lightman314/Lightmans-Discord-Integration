package io.github.lightman314.lightmansconsole.discord.links;

import java.util.UUID;

import io.github.lightman314.lightmansconsole.util.PlayerUtil;
import net.minecraft.world.entity.player.Player;

public class LinkedAccount {

	public final UUID playerID;
	public String getName() { return PlayerUtil.playerName(this.playerID); }
	public final String discordID;

	public LinkedAccount(UUID id, String discordID) {
		this.playerID = id;
		this.discordID = discordID;
	}

	public boolean equalsPlayer(Player player) {
		return player.getUUID().equals(this.playerID);
	}
	
	public boolean equalsPlayerName(String playerName)
	{
		return this.getName().toLowerCase().contentEquals(playerName.toLowerCase());
	}

	public boolean equalsDiscordID(String discordID) {
		return this.discordID.contentEquals(discordID);
	}

}
