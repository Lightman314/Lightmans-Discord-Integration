package io.github.lightman314.lightmansdiscord.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmansdiscord.LDIConfig;
import io.github.lightman314.lightmansdiscord.api.jda.data.*;
import io.github.lightman314.lightmansdiscord.api.jda.data.channels.*;
import io.github.lightman314.lightmansdiscord.api.jda.data.messages.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.minecraft.ChatFormatting;

public class MessageUtil {

	public static void sendTextMessage(MessageChannel channel, String message)
	{
		if(channel == null)
			return;

		message = MessageUtil.clearFormatting(message);

		splitMessage(message).forEach(val -> {
			if(!val.isEmpty())
				channel.sendMessage(val).queue();
		});

	}

	public static void sendTextMessage(MessageChannel channel, List<String> messages)
	{
		if(channel == null)
			return;

		messages.replaceAll(MessageUtil::clearFormatting);

		combineMessages(messages).forEach(val -> {
			if(!val.isEmpty())
				channel.sendMessage(val).queue();
		});
	}

	public static void sendPrivateMessage(User user, String message)
	{
		user.openPrivateChannel().queue((channel) -> splitMessage(message).forEach(val -> {
			if(!val.isEmpty())
				channel.sendMessage(val).queue();
		}));
	}

	public static void sendPrivateMessage(User user, List<String> messages)
	{
		user.openPrivateChannel().queue((channel) -> combineMessages(messages).forEach(val -> {
			if(!val.isEmpty())
				channel.sendMessage(val).queue();
		}));
	}

	public static List<String> splitMessage(final String message)
	{
		List<String> output = new ArrayList<>();
		if(message.length() <= 2000)
		{
			output.add(message);
			return output;
		}
		else
		{
			String[] split = message.split("\n");
			StringBuilder bufferString = new StringBuilder();
			for(String s : split)
			{
				if((bufferString + s + "\n").length() > 2000)
				{
					output.add(bufferString.toString());
					bufferString = new StringBuilder(s + "\n");
				}
				else
					bufferString.append(s).append("\n");
			}
			output.add(bufferString.toString());
		}
		return output;
	}

	public static List<String> combineMessages(final List<String> messages)
	{
		List<String> output = new ArrayList<>();
		if(messages.size() < 2)
			return messages;
		StringBuilder bufferString = new StringBuilder();
		for(String s : messages)
		{
			if((bufferString + "\n" + s).length() > 2000)
			{
				output.add(bufferString.toString());
				bufferString = new StringBuilder(s + "\n");
			}
			else
				bufferString.append(s).append("\n");
		}
		output.add(bufferString.toString());

		return output;
	}


	public static String clearFormatting(String message) {
		StringWriter result = new StringWriter();
		for(int i = 0; i < message.length(); ++i)
		{
			char c = message.charAt(i);
			if(c == ChatFormatting.PREFIX_CODE)
				i++; //Skip this char, and the following char
			else
				result.append(c);
		}
		return result.toString();
	}


	/**
	 * Formats a minecraft message to convert @discordUser @discordRole & #channelName into appropriate ping formats.
	 * @param minecraftMessage The message the player typed into the chat box.
	 * @param guild The discord guild from which to search for players, roles, and channels from.
	 * @return The newly formatted message ready to be sent as a discord message.
	 */
	public static String formatMinecraftMessage(String minecraftMessage, @Nullable SafeGuildReference guild)
	{
		if(!LDIConfig.SERVER.chatAllowPingEveryone.get())
			minecraftMessage = minecraftMessage.replaceAll("@everyone", "@**everyone**");
		if(guild == null)
			return minecraftMessage;
		int indexOffset = 0;
		//Replace @User & @Role with ping formatted text
		while(minecraftMessage.substring(indexOffset).contains("@"))
		{
			int indexof = minecraftMessage.substring(indexOffset).indexOf('@') + indexOffset;
			indexOffset = indexof + 1;
			//Get the front & back of the message that ignores the
			String firstHalf = minecraftMessage.substring(0, indexof);
			String[] splitText = minecraftMessage.substring(indexOffset).split(" ", 2);
			String secondHalf = "";
			if(splitText.length > 1)
				secondHalf = " " + splitText[1];
			String pingText = splitText[0];
			String[] splitPing = pingText.split("#", 2);
			String pingName = splitPing[0];
			String pingDiscriminator = "";
			if(splitPing.length > 1)
				pingDiscriminator = splitPing[1];

			SafeMemberReference foundMember = null;
			//Search through members by effective name
			List<SafeMemberReference> members = guild.getMembersByEffectiveName(pingName);
			if(members.size() == 1)
				foundMember = members.get(0);
			else
			{
				//If none found (or multiple found), search by username & instead if needed
				members = guild.getMembersByName(pingName);
				if(members.size() == 1)
				{
					foundMember = members.get(0);
				}
				//Use discriminator to differentiate between different users of the same username
				else if(members.size() > 1 && !pingDiscriminator.isEmpty())
				{
					for(int i = 0; i < members.size() && foundMember == null; i++)
					{
						if(members.get(i).getDiscriminator().equals(pingDiscriminator))
							foundMember = members.get(i);
					}
				}
			}

			if(foundMember != null)
			{
				minecraftMessage = firstHalf + "<@!" + foundMember.getID() + ">" + secondHalf;
				indexOffset += foundMember.getID().length();
			}
			else //Search for role of type
			{
				SafeRoleReference foundRole = null;
				List<SafeRoleReference> roles = guild.getRoles();
				for(int i = 0; i < roles.size() && foundRole == null; i++)
				{
					if(roles.get(i).getName().equals(pingName))
						foundRole = roles.get(i);
				}

				if(foundRole != null)
				{
					minecraftMessage = firstHalf + "<@&" + foundRole.getID() + ">" + secondHalf;
					indexOffset += foundRole.getID().length();
				}
			}

		}
		indexOffset = 0;
		while(minecraftMessage.substring(indexOffset).contains("#"))
		{
			int indexof = minecraftMessage.substring(indexOffset).indexOf('#') + indexOffset;
			indexOffset = indexof + 1;
			//Get the front & back of the message that ignores the
			String firstHalf = minecraftMessage.substring(0, indexof);
			String[] splitText = minecraftMessage.substring(indexOffset).split(" ", 2);
			String secondHalf = "";
			if(splitText.length > 1)
				secondHalf = " " + splitText[1];
			String channelName = splitText[0];

			List<SafeTextChannelReference> channels = guild.getChannels();
			SafeTextChannelReference foundChannel = null;
			for(int i = 0; i < channels.size() && foundChannel == null; i++)
			{
				if(channels.get(i).getName().equals(channelName))
					foundChannel = channels.get(i);
			}
			if(foundChannel != null)
			{
				minecraftMessage = firstHalf + "<#" + foundChannel.getID() + ">" + secondHalf;
				indexOffset += foundChannel.getID().length();
			}
		}
		return minecraftMessage;
	}

	/**
	 * Decodes a discord message into a text component format to be sent in minecraft's chat.
	 * @param message The discord message to convert.
	 * @param guild The discord guild from which to search for players, roles, and channels from.
	 * @return The discord message now formatted as a minecraft text component for display in-game.
	 */
	public static String formatMessageText(SafeMessageReference message, SafeGuildReference guild)
	{
		String rawMessage = message.getRaw();
		StringBuilder messageText = new StringBuilder();
		int currentModifier = 0;
		for(int i = 0; i < rawMessage.length(); i++)
		{
			String partialMessage = rawMessage.substring(i);
			if(partialMessage.startsWith("***"))
			{
				if(currentModifier == 3)
				{
					currentModifier = 0;
					messageText.append(ChatFormatting.PREFIX_CODE + "r");
					i += 2;
				}
				else if(partialMessage.substring(1).contains("***") && currentModifier == 0)
				{
					currentModifier = 3;
					messageText.append(ChatFormatting.PREFIX_CODE + "l" + ChatFormatting.PREFIX_CODE + "o");
					i += 2;
				}
				else
					messageText.append(partialMessage.charAt(0));
			}
			else if(partialMessage.startsWith("**"))
			{
				if(currentModifier == 2)
				{
					currentModifier = 0;
					messageText.append(ChatFormatting.PREFIX_CODE + "r");
					i++;
				}
				else if(partialMessage.substring(1).contains("**") && currentModifier == 0)
				{
					currentModifier = 2;
					messageText.append(ChatFormatting.PREFIX_CODE + "l");
					i++;
				}
				else
					messageText.append(partialMessage.charAt(0));
			}
			else if(partialMessage.startsWith("*"))
			{
				if(currentModifier == 1)
				{
					currentModifier = 0;
					messageText.append(ChatFormatting.PREFIX_CODE + "r");
				}
				else if(partialMessage.substring(1).contains("*") && currentModifier == 0)
				{
					currentModifier = 1;
					messageText.append(ChatFormatting.PREFIX_CODE + "o");
				}
				else
					messageText.append(partialMessage.charAt(0));
			}
			else
				messageText.append(partialMessage.charAt(0));
		}
		//Check for pings, etc
		if(guild != null)
		{
			int indexOffset = 0;
			while(messageText.substring(indexOffset).contains("<"))
			{
				int startIndex = messageText.substring(indexOffset).indexOf('<') + indexOffset;
				indexOffset = startIndex + 1;
				int endIndex = messageText.substring(indexOffset).indexOf('>') + indexOffset;
				if(endIndex >= 0)
				{
					//Get the front, middle, and back parts of the message
					String frontText = messageText.substring(0, startIndex);
					String processText = messageText.substring(startIndex, endIndex + 1);
					String endText = "";
					if(endIndex < messageText.length())
						endText = messageText.substring(endIndex + 1);

					//Process the middle portion
					if(processText.startsWith("<@&"))
					{
						String roleId = processText.substring(3, processText.length() - 1);
						SafeRoleReference foundRole = guild.getRole(roleId);
						if(foundRole != null)
							messageText = new StringBuilder(frontText + "@" + foundRole.getName() + endText);
					}
					else if(processText.startsWith("<@"))
					{
						//Process ping
						SafeMemberReference foundMember = MemberUtil.getMemberFromPing(guild, processText);
						if(foundMember != null)
							messageText = new StringBuilder(frontText + "@" + foundMember.getEffectiveName() + endText);
					}
					else if(processText.startsWith("<#"))
					{
						String channelId = processText.substring(2, processText.length() - 1);
						SafeTextChannelReference foundChannel = guild.getChannel(channelId);
						if(foundChannel != null)
							messageText = new StringBuilder(frontText + "#" + foundChannel.getName() + endText);
					}

				}

			}
		}

		return messageText.toString();
	}

}
