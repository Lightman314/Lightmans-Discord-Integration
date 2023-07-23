package io.github.lightman314.lightmansdiscord.discord.links;

import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import io.github.lightman314.lightmansdiscord.api.jda.JDAUtil;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeMemberReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeUserReference;
import io.github.lightman314.lightmansdiscord.util.PlayerUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.minecraft.world.entity.player.Player;

public class LinkedAccount {

	public final UUID playerID;
	public String getName() { return PlayerUtil.playerName(this.playerID); }
	public final String discordID;
	public SafeUserReference getUser() { return JDAUtil.getUser(this.discordID); }
	public SafeMemberReference getMember() {
		List<Guild> guilds = LightmansDiscordIntegration.PROXY.getJDA().getGuilds();
		for (Guild guild : guilds) {
			Member m = guild.getMemberById(this.discordID);
			if (m != null)
				return SafeMemberReference.of(m);
		}
		return null;
	}
	public String getMemberName()
	{
		SafeMemberReference m = this.getMember();
		if(m != null)
			return m.getEffectiveName();
		SafeUserReference u = this.getUser();
		if(u != null)
			return u.getName();
		return "ERROR";
	}

	public LinkedAccount(UUID id, String discordID) {
		this.playerID = id;
		this.discordID = discordID;
	}

	public boolean equalsPlayer(Player player) { return player.getUUID().equals(this.playerID); }
	
	public boolean equalsPlayerName(String playerName) { return this.getName().toLowerCase().contentEquals(playerName.toLowerCase()); }

	public boolean equalsDiscordID(String discordID) { return this.discordID.contentEquals(discordID); }

}
