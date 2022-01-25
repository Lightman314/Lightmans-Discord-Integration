package io.github.lightman314.lightmansconsole.message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class MessageManager {
	
	//Master Entry List
	private static final List<MessageEntry> ENTRIES = Lists.newArrayList();
	private static final File MESSAGE_FILE = new File("config/lightmansdiscord_messages.txt");

	//public static final MessageEntry M_DUMMY = MessageEntry.create("dummy", "Comment", "Value", "arg1");
	
	//Chat message formats
	public static final MessageEntry M_FORMAT_MINECRAFT_PREFIX = MessageEntry.create("chat_minecraft_pre", "Prefix portion of discord messages in minecraft. Displayed as {prefix}{user}{postfix} {message}, where this is the prefix.", "[DISCORD] <");
	public static final MessageEntry M_FORMAT_MINECRAFT_POSTFIX = MessageEntry.create("chat_minecraft_post", "Prefix portion of discord messages in minecraft. Displayed as {prefix}{user}{postfix} {message}, where this is the postfix.", ">");
	public static final MessageEntry M_FORMAT_DISCORD = MessageEntry.create("chat_discord", "Format of minecraft messages in discord.\n{player} for player name.\n{message} for chat message.","**{player}:** {message}", "player","message");
	public static final MessageEntry M_PLAYER_JOIN = MessageEntry.create("chat_playerjoin", "Format of player join message in discord.\n{minecraft} for default minecraft translation.\n{player} for player name.", "{minecraft}", "minecraft","player");
	public static final MessageEntry M_PLAYER_LEAVE = MessageEntry.create("chat_playerleave", "Format of player leave message in discord.\n{minecraft} for default minecraft translation.\n{player} for player name.", "{minecraft}", "minecraft","player");
	public static final MessageEntry M_PLAYER_ACHIEVEMENT = MessageEntry.create("chat_achievement", "Format of the achievement announcement in discord.\n{player} for the player.\n{achievename} for the achievement's name.\n{achievedesc} for the achievements description.", "{player} hast made the achievement **{achievename}**\\n*{achievedesc}*", "player","achievename","achievedesc");
	
	//Console bot messages
	public static final MessageEntry M_CONSOLEBOT_READY = MessageEntry.create("consolebot_ready", "Message sent when the console bot is ready for commands.", "Console Bot is ready!");
	
	//Boot/stop messages
	public static final MessageEntry M_SERVER_BOOT = MessageEntry.create("chat_server_boot", "Message sent when the server starts booting, and the bot is loaded.", "Server is booting!");
	public static final MessageEntry M_SERVER_READY = MessageEntry.create("chat_server_ready", "Message sent when the server is done booting.", "Server is ready for players!");
	public static final MessageEntry M_SERVER_STOP = MessageEntry.create("chat_server_stop", "Message sent when the server is done booting.", "Server has stopped.");
	
	//Topic formats
	public static final MessageEntry M_TOPIC_TEXT = MessageEntry.create("topic_text","Format of the server chats topic.\n{playerCount} for online player count.\n{playerLimit} for player limit.", "There are {playerCount} players online.","playerCount","playerLimit");
	public static final MessageEntry M_TOPIC_BOOT = MessageEntry.create("topic_booting", "Topic text while the server is booting.", "Server is booting.");
	public static final MessageEntry M_TOPIC_OFFLINE = MessageEntry.create("topic_offline", "Topic text when the server is stopped. May not always trigger before the bot is stopped.", "Server is offline.");
	
	//Activity formats
	public static final MessageEntry M_ACTIVITY_TEXT = MessageEntry.create("activity_text", "Format of the bots activity text.\n{playerCount} for online player count.\n{playerLimit} for player limit.", "{playerCount} players online", "playerCount", "playerLimit");
	public static final MessageEntry M_ACTIVITY_BOOT = MessageEntry.create("activity_booting", "Activity text while the server is booting.", "Server is booting.");
	public static final MessageEntry M_ACTIVITY_OFFLINE = MessageEntry.create("activity_offline", "Activity text when the server is stopped. May not always trigger before the bot is stopped.", "Server is offline.");
	
	//Discord command outputs
	public static final MessageEntry M_ERROR_PERMISSIONS = MessageEntry.create("error_permissions", "Error message sent when a player attempts to run a command they don't have permission for.", "You do not have permission to run that command.");
	public static final MessageEntry M_ERROR_NOPING = MessageEntry.create("error_noping", "Error message sent when the player doesn't ping a user when running a command that requires it.", "No user was pinged.");
	public static final MessageEntry M_ERROR_PING = MessageEntry.create("error_ping", "Error message sent when the bot was unable to extract the user from the ping.", "Error extracting user from ping. Did they leave the discord server?");
	public static final MessageEntry M_ERROR_NOTLINKED = MessageEntry.create("error_notlinked", "Error message sent when a command requiring a linked user was not linked to a minecraft account.\n{user} for the pinged users name.", "{user} is not linked to a minecraft account.", "user");
	public static final MessageEntry M_ERROR_NOTLINKEDSELF = MessageEntry.create("error_notlinked_self", "Error message sent when a command requiring the executor to be linked to a minecraft account, but they aren't linked.", "Your account is not linked to a minecraft account.");
	
	//!linkuser
	public static final MessageEntry M_LINKUSER_PLAYERLINKED = MessageEntry.create("command_linkuser_playerlinked", "Error message sent when !linkuser is run on an already linked minecraft player.\n{player} for the players name.", "'{player}' is already linked to a discord account.", "player");
	public static final MessageEntry M_LINKUSER_USERLINKED = MessageEntry.create("command_linkuser_userlinked", "Error message sent when !linkuser is run on an already linked discord account.\n{user} for their discord accounts name.\n{player} for their linked minecraft account name.", "'{user}' is already linked to {player}.", "user", "player");
	public static final MessageEntry M_LINKUSER_USERPENDING = MessageEntry.create("command_linkuser_userpending", "Error message sent when !linkuser is run on a discord account with a pending link.\n{user} for their discord accounts name.", "'{user}' already has a pending link.", "user");
	public static final MessageEntry M_LINKUSER_NO_ACCOUNT = MessageEntry.create("command_linkuser_noaccount", "Error message sent when !linkuser is run with a non-existent minecraft account.\n{player} for the invalid minecraft name.", "'{player}' is not a valid Minecraft account.", "player");
	public static final MessageEntry M_LINKUSER_SUCCESS = MessageEntry.create("command_linkuser_success", "Message sent when !linkuser is run successfully.\n{player} for the players name.\n{user} for their discord accounts name.", "Successfully linked {user} to '{player}'", "user", "player");
	public static final MessageEntry M_LINKUSER_WELCOME = MessageEntry.create("command_linkuser_welcome", "PM sent to users linked by the !linkuser command. New lines can be defined by a \\n", "");
	//!unlinkplayer
	public static final MessageEntry M_UNLINKPLAYER_FAIL = MessageEntry.create("command_unlinkplayer_fail", "Message sent when !unlinkplayer fails to find a linked account for the given player.\n{player} for the input player name.", "'{player}' is not linked to any accounts.", "player");
	public static final MessageEntry M_UNLINKPLAYER_SUCCESS = MessageEntry.create("command_unlinkplayer_success", "Message sent when !unlinkplayer is run successfully.\n{player} for the players name.\n{user} for their discord accounts name.", "'{player}' has been unlinked from {user}'s account.", "player", "user");
	//!link
	public static final MessageEntry M_LINK_FAIL = MessageEntry.create("command_link_fail", "Message sent when !link failes due to their account already being linked to an account.", "Your discord account is already linked to an account.");
	public static final MessageEntry M_LINK_SUCCESS = MessageEntry.create("command_link_success", "Message sent in the public channel when !link is run successfully.", "Your link key has been sent to you via private message.");
	public static final MessageEntry M_LINK_MESSAGE = MessageEntry.create("command_link_message", "Message sent in a PM to the player when !link is run successfully.\nMust contain the {linkkey} text.\n{linkkey} for the link key.\n{command} for the mincraft command suggestion '/link {linkkey}'", "Your link key is '{linkkey}'.\\nLog in to the server and run '{command}' to finish linking your account." ,"linkkey","command");
	//!unlink
	public static final MessageEntry M_UNLINK_FAIL = MessageEntry.create("command_unlink_fail", "Message sent when !unlink fails to find a linked account for the player.", "Your discord account is not linked to a minecraft account on this server.");
	public static final MessageEntry M_UNLINK_SUCCESS = MessageEntry.create("command_unlink_success", "Message sent when !unlink successfully unlinks the player from their minecraft account.\n{player} for their minecraft name.", "Your discord account has been successfully unlinked from '{player}'.", "player");
	public static final MessageEntry M_UNLINK_PENDING = MessageEntry.create("command_unlink_success_pending", "Message sent when !unlink successfully unlinks the player from a pending link.", "You discord accounts pending link has been removed.");
	//!ign
	public static final MessageEntry M_IGN_SUCCESS = MessageEntry.create("command_ign_success", "Message sent when !ign is run successfully.", "{user} is linked to {player}", "user", "player");
	//!discordname
	public static final MessageEntry M_DISCORDNAME_SUCCESS = MessageEntry.create("command_discordname_success", "Message sent when !discordname is run successfully.\n{player} for their minecraft name.\n{user} for their discord name.", "'{player}' is linked to {member}", "player", "user");
	public static final MessageEntry M_DISCORDNAME_FAIL = MessageEntry.create("command_discordname_fail", "Message sent when !discordname could not find a linked account for the given player.\n{player} for their minecraft name.", "'{player}' is not linked to a discord account.", "player");
	
	//!help
	public static final MessageEntry M_HELP_HELP = MessageEntry.create("help_help", "Help message for !help.", "Show this help info.");
	public static final MessageEntry M_HELP_LINK = MessageEntry.create("help_link", "Help message for !link.", "Generate a link key to start the linking process.");
	public static final MessageEntry M_HELP_UNLINK = MessageEntry.create("help_unlink", "Help message for !unlink.", "Unlink your discord account from your minecraft username.");
	public static final MessageEntry M_HELP_LINKUSER = MessageEntry.create("help_linkuser", "Help message for !linkuser.", "Links the replied pinged users account to the given minecraft username.");
	public static final MessageEntry M_HELP_UNLINKPLAYER = MessageEntry.create("help_unlinkplayer", "Help message for !unlinkplayer.", "Unlinks the given minecraft user from their discord account.");
	public static final MessageEntry M_HELP_DISCORDLIST = MessageEntry.create("help_discordlist", "Help message for !discordlist.", "Lists data about every linked minecraft/discord account. **WARNING: SENSITIVE DATA, DO NOT RUN IN A PUBLIC CHANNEL!**");
	public static final MessageEntry M_HELP_DISCORDNAME = MessageEntry.create("help_discordname", "Help message for !discordname.", "Get the discord members name of the given minecraft account.");
	public static final MessageEntry M_HELP_IGN = MessageEntry.create("help_ign", "Help message for !ign.", "Get the ign of the pinged users minecraft account.");
	
	//Lightman's Currency Bot
	//!notifications help
	public static final MessageEntry M_NOTIFICATIONS_ENABLED = MessageEntry.create("command_notifications_enabled", "Message sent when running !messages help while notifications are enabled.", "Personal notifications are enabled.");
	public static final MessageEntry M_NOTIFICATIONS_DISABLED = MessageEntry.create("command_notifications_disabled", "Message sent when running !messages help while notifications are disabled.", "Personal notifications are disabled.");
	public static final MessageEntry M_NOTIFICATIONS_HELP = MessageEntry.create("command_notifications_help", "Remaining message sent when running !messages help.", "If personal notifications are enabled you will receive notifications for the following:\\n-Purchases made on traders you own.\\n\"-When your trader is out of stock.");
	//!notifications enable
	public static final MessageEntry M_NOTIFICATIONS_ENABLE_SUCCESS = MessageEntry.create("command_notifications_enable_successs", "Message sent when running !messages enable successfully.", "Personal notifications are now enabled.");
	public static final MessageEntry M_NOTIFICATIONS_ENABLE_FAIL = MessageEntry.create("command_notifications_enable_fail", "Message sent when failing to run !messages enable.", "Personal notifications were already enabled.");
	//!notifications disable
	public static final MessageEntry M_NOTIFICATIONS_DISABLE_SUCCESS = MessageEntry.create("command_notifications_disable_successs", "Message sent when running !messages disable successfully.", "Personal notifications are now disabled.");
	public static final MessageEntry M_NOTIFICATIONS_DISABLE_FAIL = MessageEntry.create("command_notifications_disable_fail", "Message sent when failing to run !messages disable.", "Personal notifications were already disabled.");
	
	//!search
	public static final MessageEntry M_SEARCH_NORESULTS = MessageEntry.create("command_search_noresults", "Message sent when !search is run and no search results were found.", "No results found.");
	
	//Trade Notification
	public static final MessageEntry M_NOTIFICATION_OUTOFSTOCK = MessageEntry.create("lightmanscurrency_notification_outofstock", "Message added to the end of a trade notification informing you that your trade is out of stock.", "**This trade is now out of stock!**");
	
	//Trader Announcement
	public static final MessageEntry M_NEWTRADER = MessageEntry.create("lightmanscurrency_newtrader", "Announcement made in the currency bot channel when a new universal trader has been made.\n{player} for the traders owner name.", "{player} has made a new Universal Trader!", "player");
	public static final MessageEntry M_NEWTRADER_NAMED = MessageEntry.create("lightmanscurrency_newtrader_named", "Announcement made in the currency bot channel when a new universal trader with a custom name has been made.\n{player} for the traders owner name.\n{trader} for the traders custom name.", "{player} has made a new Universal Trader '{trader}'!", "player", "trader");
	
	//Lightman's Currency !help
	public static final MessageEntry M_HELP_LC_NOTIFICATIONS = MessageEntry.create("help_lc_notifications", "Help message for lightman's currency !notifications.", "Handle private currency notifications.");
	public static final MessageEntry M_HELP_LC_SEARCH1 = MessageEntry.create("help_lc_search1", "Help message for lightman's currency !search <sales|purchases|barters|all>.", "List all universal trades selling items containing the searchText. Leave searchText empty to see all sales/purchases/barters.");
	public static final MessageEntry M_HELP_LC_SEARCH2 = MessageEntry.create("help_lc_search2", "Help message for lightman's currency !search <players|shops>.", "List all trades for universal traders with player/shop names containing the searchText. Leave searchText empty to see all traders trades.");
	
	//Command outputs
	//'/linkdiscord'
	public static final MessageEntry M_COMMAND_LINK_COMPLETE = MessageEntry.create("command_linkdiscord_complete", "Message displayed when the player successfully runs the /linkdiscord [linkkey] command.\n{user} for their linked discord name.", "Your account has been successfully linked to your discord account.", "user");
	public static final MessageEntry M_COMMAND_LINK_BADKEY = MessageEntry.create("command_linkdiscord_badkey", "Message displayed when the player runs the /linkdiscord [linkkey] command with an invalid key.\n{linkkey} for the invalid link key.", "{linkkey} is not a valid link key.", "linkkey");
	public static final MessageEntry M_COMMAND_LINK_ALREADYLINKED = MessageEntry.create("command_linkdiscord_alreadylinked", "Message displayed when the player runs the /linkdiscord [linkkey] command when their minecraft account is already linked to a discord account.\n{user} for their linked discord name.", "Your account is already linked to a discord account.", "user");
	//'/unlinkdiscord'
	public static final MessageEntry M_COMMAND_UNLINK_COMPLETE = MessageEntry.create("command_unlinkdiscord_complete", "Message displayed when the player successfully runs the /unlinkdiscord command.\n{user} for their formerly linked discord name.", "You are no longer linked to {user}.", "user");
	public static final MessageEntry M_COMMAND_UNLINK_FAILED = MessageEntry.create("command_unlinkdiscord_fail", "Message displayed when the player runs the /unlinkdiscord command when they're not linked.", "Your account is not linked to a discord account.");
	
	public static void reload()
	{
		//Load the entries from the file
		readEntries(MESSAGE_FILE);
		//Re-write the file to fix any missing entries, update comments, etc.
		writeFile(MESSAGE_FILE);
		
	}
	
	private static List<String> readLines(File messageFile)
	{
		if(!messageFile.exists())
			return Lists.newArrayList();
		try {
			BufferedReader br = new BufferedReader(new FileReader(messageFile));
			List<String> lines = Lists.newArrayList();
			String line;
			while((line = br.readLine()) != null)
			{
				lines.add(line);
			}
			br.close();
			return lines;
		} catch(IOException e) {
			LightmansDiscordIntegration.LOGGER.error("Error reading message file.", e.getMessage());
			return Lists.newArrayList();
		}
	}
	
	//Also adds 
	private static void readEntries(File messageFile)
	{
		//Read the lines from the file.
		List<String> lines = readLines(messageFile);
		List<MessageEntry> missedEntries = Lists.newArrayList(ENTRIES);
		
		//Read entries from the lines
		for(String line : lines)
		{
			if(line.startsWith("#") || line.isEmpty())
				continue;
			if(line.contains("="))
			{
				String[] splitLine = line.split("=", 2);
				String key = splitLine[0];
				String value = splitLine[1];
				MessageEntry entry = getEntry(key);
				if(entry != null)
				{
					//Load the entry
					entry.currentValue = value;
					//Remove the entry from the missed entry list
					missedEntries.remove(entry);
				}
				else //Warn about the unexpected entry.
					LightmansDiscordIntegration.LOGGER.warn("Found unknown key '" + key + "' while loading the message file. Unexpected entry will be ignored.");
			}
		}
		
		//Reset the missed entries to their default values
		missedEntries.forEach(entry -> {
			entry.currentValue = entry.defaultValue;
			LightmansDiscordIntegration.LOGGER.warn("Message File was missing entry for '" + entry.key + "'. Resetting to default value.");
		});
	}
	
	private static void writeFile(File messageFile)
	{
		try {
			if(!messageFile.exists())
			{
				File folder = new File(messageFile.getParent());
				if(!folder.exists())
					folder.mkdirs();
				
				if(!messageFile.createNewFile())
				{
					LightmansDiscordIntegration.LOGGER.error("Unable to create the message file.");
					return;
				}
			}
			
			FileWriter fw = new FileWriter(messageFile, false);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter writer = new PrintWriter(bw);
			
			//Initial comments
			writer.println("#Message Inputs for Lightman's Discord Integration.");
			writer.println("#Type \\n for a new line. Some messages will have optional inputs surrounded by {} to be filled in with various data.");
			writer.println("#Each message should list the optional inputs, and what they'll be replaced by.");
			writer.println();
			
			for(int i = 0; i < ENTRIES.size(); ++i)
			{
				MessageEntry entry = ENTRIES.get(i);
				List<String> commentLines = Lists.newArrayList(entry.comment.split("\n"));
				commentLines.forEach(comment -> writer.println("#" + comment));
				writer.println(entry.key + "=" + entry.currentValue);
				writer.println();
			}
			writer.close();
			
		} catch(IOException e) {
			LightmansDiscordIntegration.LOGGER.error("Error modifying the message file.");
			e.printStackTrace();
		}
		
	}

	private static MessageEntry getEntry(String key)
	{
		for(int i = 0; i < ENTRIES.size(); ++i)
		{
			if(ENTRIES.get(i).key.contentEquals(key))
				return ENTRIES.get(i);
		}
		return null;
	}
	
	public static class MessageEntry
	{
		public final String key;
		public final String comment;
		public final String defaultValue;
		
		private String currentValue;
		
		private final List<String> formatKeys;
		
		public String get() { return format(); }
		
		public String format(Object... format)
		{
			String result = currentValue;
			//Replace \n with a new line
			result = result.replace("\\n", "\n");
			for(int i = 0; i < formatKeys.size() && i < format.length; ++i)
			{
				String formatText = format[i].toString();
				if(format[i] instanceof Component) //Check if it's a text component, and if so, run .getString() instead of .toString();
					formatText = ((Component)format[i]).getString();
				result = result.replace("{" + formatKeys.get(i) + "}", formatText);
			}
				
			return result;
		}
		
		public Component formatComponent(Object... format)
		{
			return new TextComponent(format(format));
		}
		
		private MessageEntry(String key, String comment, String defaultValue, String... formatKeys)
		{
			this.key = key; this.comment = comment; this.defaultValue = this.currentValue = defaultValue;
			this.formatKeys = Lists.newArrayList(formatKeys);
		}
		
		public static MessageEntry create(String key, String comment, String defaultValue, String... formatKeys)
		{
			MessageEntry entry = new MessageEntry(key, comment, defaultValue, formatKeys);
			if(getEntry(entry.key) != null)
				LightmansDiscordIntegration.LOGGER.warn("Duplicate message key '" + key + "' was attempted to be created. Duplicate was ignored.");
			else
				ENTRIES.add(entry);
			return entry;
		}
		
	}
	
}