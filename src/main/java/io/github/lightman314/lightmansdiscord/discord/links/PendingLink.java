package io.github.lightman314.lightmansdiscord.discord.links;

import java.util.UUID;

public class PendingLink {

	public final String linkKey;
	public final String userID;
	
	public PendingLink(String linkKey, String userID)
	{
		this.linkKey = linkKey;
		this.userID = userID;
	}
	
	public PendingLink(String userID)
	{
		this.linkKey = UUID.randomUUID().toString();
		this.userID = userID;
	}
	
}
