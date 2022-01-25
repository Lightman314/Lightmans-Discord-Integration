package io.github.lightman314.lightmansconsole.discord.links;

import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.util.PlayerUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.world.entity.player.Player;

public class LinkedAccount {

	public final UUID playerID;
	public String getName() { return PlayerUtil.playerName(this.playerID); }
	public final String discordID;
	public User getUser() { return LightmansDiscordIntegration.PROXY.getJDA().getUserById(this.discordID); }
	public Member getMember() {
		List<Guild> guilds = LightmansDiscordIntegration.PROXY.getJDA().getGuilds();
		for(int i = 0; i < guilds.size(); ++i)
		{
			Member m = guilds.get(i).getMemberById(this.discordID);
			if(m != null)
				return m;
		}
		return null;
	}
	public String getMemberName()
	{
		Member m = this.getMember();
		if(m != null)
			return m.getEffectiveName();
		return "ERROR";
	}

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
