package io.github.lightman314.lightmansconsole.discord.links;

public class PartialLinkedAccount {

	public final String discordID;
	public final String playerName;
	
	public PartialLinkedAccount(String playerName, String discordID)
	{
		this.discordID = discordID;
		this.playerName = playerName;
	}
	
}
