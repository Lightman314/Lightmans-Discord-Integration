package io.github.lightman314.lightmansconsole.util;

import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class MemberUtil {

	public static String getUserIdFromPing(String pingText)
	{
		//Ping can be formatted as <@!USER_ID> or as <@USER_ID>. Will need to confirm which one it is.
		int startIndex = pingText.indexOf("<@");
		if(startIndex >= 0)
		{
			String ping = pingText.substring(startIndex);
			int endIndex = ping.indexOf('>');
			if(endIndex >= 0)
			{
				String id = ping.substring(2, endIndex);
				if(!"0123456789".contains(id.substring(0,1))) //If the first character of the extracted id is not a number, then it must've been a <@! format ping
					id = id.substring(1);
				LightmansDiscordIntegration.LOGGER.info("Extracted '" + id + "' from '" + pingText + "'");
				return id;
			}
			else
				LightmansDiscordIntegration.LOGGER.warn("'" + pingText + "' has no '>'");
		}
		else
			LightmansDiscordIntegration.LOGGER.warn("'" + pingText + "' does not start with '<@!'");
		return "";
	}
	
	public static User getUserFromPing(JDA jda, String pingText)
	{
		String id = getUserIdFromPing(pingText);
		User user = id.isEmpty() ? null : jda.getUserById(id);
		if(user == null && !id.isEmpty())
			LightmansDiscordIntegration.LOGGER.warn("No guild member could be found with id '" + id + "'");
		else if(user != null)
			LightmansDiscordIntegration.LOGGER.info("Found member with id '" + id + "'");
		return user;
	}
	
	public static Member getMemberFromPing(Guild guild, String pingText)
	{
		String id = getUserIdFromPing(pingText);
		Member member = id.isEmpty() ? null : guild.getMemberById(id);
		if(member == null && !id.isEmpty())
			LightmansDiscordIntegration.LOGGER.warn("No guild member could be found with id '" + id + "'");
		else if(member != null)
			LightmansDiscordIntegration.LOGGER.info("Found member with id '" + id + "'");
		return member;
	}
	
}
