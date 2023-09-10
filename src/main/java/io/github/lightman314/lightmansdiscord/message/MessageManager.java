package io.github.lightman314.lightmansdiscord.message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import io.github.lightman314.lightmansdiscord.events.LoadMessageEntriesEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MessageManager {

	private MessageManager() {}

	//Master Entry List
	private static ImmutableList<io.github.lightman314.lightmansdiscord.message.MessageEntry> ENTRIES = ImmutableList.of();

	private static final List<io.github.lightman314.lightmansdiscord.message.MessageEntry> LOCAL_ENTRIES = new ArrayList<>();
	private static final File MESSAGE_FILE = new File("config/lightmansdiscord_messages.txt");

	//public static final MessageEntry M_DUMMY = MessageEntry.create(LOCAL_ENTRIES, "dummy", "Comment", "Value", "arg1");
	
	//Chat message formats
	public static final MessageEntry M_FORMAT_MINECRAFT = MessageEntry.create(LOCAL_ENTRIES, "chat_minecraft", "How discord messages appear in minecraft.\n{user} for the discord users formatted name.\n{message} for the message sent.", "[DISCORD] <{user}> {message}", "message");
	public static final MessageEntry M_FORMAT_MINECRAFT_BOT = MessageEntry.create(LOCAL_ENTRIES, "chat_minecraft_bot", "How discord bot messages appear in minecraft.\n{bot} for the bots name.\n{message} for the message sent. Should contain this under all circumstances.", "[{bot}] {message}", "message");
	public static final MessageEntry M_FORMAT_DISCORD = MessageEntry.create(LOCAL_ENTRIES, "chat_discord", "Format of minecraft messages in discord.\n{player} for player name.\n{message} for chat message.","**{player}:** {message}", "player","message");
	public static final MessageEntry M_PLAYER_JOIN = MessageEntry.create(LOCAL_ENTRIES, "chat_playerjoin", "Format of player join message in discord.\n{minecraft} for default minecraft translation.\n{player} for player name.", "{minecraft}", "minecraft","player");
	public static final MessageEntry M_PLAYER_LEAVE = MessageEntry.create(LOCAL_ENTRIES, "chat_playerleave", "Format of player leave message in discord.\n{minecraft} for default minecraft translation.\n{player} for player name.", "{minecraft}", "minecraft","player");
	public static final MessageEntry M_PLAYER_LIST = MessageEntry.create(LOCAL_ENTRIES, "chat_playerlist_starter", "Format of the title shown above the player list when running the !list command in the chat channel.\n{count} for the number of players online.", "count");
	public static final MessageEntry M_PLAYER_ACHIEVEMENT = MessageEntry.create(LOCAL_ENTRIES, "chat_achievement", "Format of the achievement announcement in discord.\n{player} for the player.\n{achievename} for the achievement's name.\n{achievedesc} for the achievements description.", "{player} hast made the achievement **{achievename}**\\n*{achievedesc}*", "player","achievename","achievedesc");
	public static final MessageEntry M_PLAYER_DEATH = MessageEntry.create(LOCAL_ENTRIES, "chat_playerdeath", "Format of the player death message in discord.\n{minecraft} for default minecraft death message.\n{player} for the player name.", "{minecraft}", "minecraft","player");
	public static final MessageEntry M_ENTITY_DEATH = MessageEntry.create(LOCAL_ENTRIES, "chat_entitydeath", "Format of the named entity death message in discord.\n{minecraft} for default minecraft death message.\n{entity} for the entity name.", "{minecraft}", "minecraft","entity");
	public static final MessageEntry M_MEMBER_HOVER = MessageEntry.create(LOCAL_ENTRIES, "chat_mention_hover", "Message shown in minecraft when hovering over a discord users name from 'chat_minecraft' and 'chat_minecraft_bot' messages.", "Mention");
	
	//Console bot messages
	public static final MessageEntry M_CONSOLEBOT_READY = MessageEntry.create(LOCAL_ENTRIES, "consolebot_ready", "Message sent when the console bot is ready for commands.", "Console Bot is ready!");
	
	//Boot/stop messages
	public static final MessageEntry M_SERVER_BOOT = MessageEntry.create(LOCAL_ENTRIES, "chat_server_boot", "Message sent when the server starts booting, and the bot is loaded.", "Server is booting!");
	public static final MessageEntry M_SERVER_READY = MessageEntry.create(LOCAL_ENTRIES, "chat_server_ready", "Message sent when the server is done booting.", "Server is ready for players!");
	public static final MessageEntry M_SERVER_STOP = MessageEntry.create(LOCAL_ENTRIES, "chat_server_stop", "Message sent when the server is done booting.", "Server has stopped.");
	
	//Topic formats
	public static final MessageEntry M_TOPIC_TEXT = MessageEntry.create(LOCAL_ENTRIES, "topic_text","Format of the server chats topic.\n{playerCount} for online player count.\n{playerLimit} for player limit.", "There are {playerCount} players online.","playerCount","playerLimit");
	public static final MessageEntry M_TOPIC_BOOT = MessageEntry.create(LOCAL_ENTRIES, "topic_booting", "Topic text while the server is booting.", "Server is booting.");
	public static final MessageEntry M_TOPIC_OFFLINE = MessageEntry.create(LOCAL_ENTRIES, "topic_offline", "Topic text when the server is stopped. May not always trigger before the bot is stopped.", "Server is offline.");
	
	//Activity formats
	public static final MessageEntry M_ACTIVITY_TEXT = MessageEntry.create(LOCAL_ENTRIES, "activity_text", "Format of the bots activity text.\n{playerCount} for online player count.\n{playerLimit} for player limit.", "{playerCount} players online", "playerCount", "playerLimit");
	public static final MessageEntry M_ACTIVITY_BOOT = MessageEntry.create(LOCAL_ENTRIES, "activity_booting", "Activity text while the server is booting.", "Server is booting.");
	public static final MessageEntry M_ACTIVITY_OFFLINE = MessageEntry.create(LOCAL_ENTRIES, "activity_offline", "Activity text when the server is stopped. May not always trigger before the bot is stopped.", "Server is offline.");
	
	//Discord command outputs
	public static final MessageEntry M_ERROR_PERMISSIONS = MessageEntry.create(LOCAL_ENTRIES, "error_permissions", "Error message sent when a player attempts to run a command they don't have permission for.", "You do not have permission to run that command.");
	public static final MessageEntry M_ERROR_NOPING = MessageEntry.create(LOCAL_ENTRIES, "error_noping", "Error message sent when the player doesn't ping a user when running a command that requires it.", "No user was pinged.");
	public static final MessageEntry M_ERROR_PING = MessageEntry.create(LOCAL_ENTRIES, "error_ping", "Error message sent when the bot was unable to extract the user from the ping.", "Error extracting user from ping. Did they leave the discord server?");
	public static final MessageEntry M_ERROR_NOTLINKED = MessageEntry.create(LOCAL_ENTRIES, "error_notlinked", "Error message sent when a command requiring a linked user was not linked to a minecraft account.\n{user} for the pinged users name.", "{user} is not linked to a minecraft account.", "user");
	public static final MessageEntry M_ERROR_NOTLINKEDSELF = MessageEntry.create(LOCAL_ENTRIES, "error_notlinked_self", "Error message sent when a command requiring the executor to be linked to a minecraft account, but they aren't linked.", "Your account is not linked to a minecraft account.");
	
	//!linkuser
	public static final MessageEntry M_LINKUSER_PLAYERLINKED = MessageEntry.create(LOCAL_ENTRIES, "command_linkuser_playerlinked", "Error message sent when !linkuser is run on an already linked minecraft player.\n{player} for the players name.", "'{player}' is already linked to a discord account.", "player");
	public static final MessageEntry M_LINKUSER_USERLINKED = MessageEntry.create(LOCAL_ENTRIES, "command_linkuser_userlinked", "Error message sent when !linkuser is run on an already linked discord account.\n{user} for their discord accounts name.\n{player} for their linked minecraft account name.", "'{user}' is already linked to {player}.", "user", "player");
	public static final MessageEntry M_LINKUSER_USERPENDING = MessageEntry.create(LOCAL_ENTRIES, "command_linkuser_userpending", "Error message sent when !linkuser is run on a discord account with a pending link.\n{user} for their discord accounts name.", "'{user}' already has a pending link.", "user");
	public static final MessageEntry M_LINKUSER_NO_ACCOUNT = MessageEntry.create(LOCAL_ENTRIES, "command_linkuser_noaccount", "Error message sent when !linkuser is run with a non-existent minecraft account.\n{player} for the invalid minecraft name.", "'{player}' is not a valid Minecraft account.", "player");
	public static final MessageEntry M_LINKUSER_SUCCESS = MessageEntry.create(LOCAL_ENTRIES, "command_linkuser_success", "Message sent when !linkuser is run successfully.\n{player} for the players name.\n{user} for their discord accounts name.", "Successfully linked {user} to '{player}'", "user", "player");
	public static final MessageEntry M_LINKUSER_WELCOME = MessageEntry.create(LOCAL_ENTRIES, "command_linkuser_welcome", "PM sent to users linked by the !linkuser command. New lines can be defined by a \\n", "");
	//!unlinkplayer
	public static final MessageEntry M_UNLINKPLAYER_FAIL = MessageEntry.create(LOCAL_ENTRIES, "command_unlinkplayer_fail", "Message sent when !unlinkplayer fails to find a linked account for the given player.\n{player} for the input player name.", "'{player}' is not linked to any accounts.", "player");
	public static final MessageEntry M_UNLINKPLAYER_SUCCESS = MessageEntry.create(LOCAL_ENTRIES, "command_unlinkplayer_success", "Message sent when !unlinkplayer is run successfully.\n{player} for the players name.\n{user} for their discord accounts name.", "'{player}' has been unlinked from {user}'s account.", "player", "user");
	//!link
	public static final MessageEntry M_LINK_FAIL = MessageEntry.create(LOCAL_ENTRIES, "command_link_fail", "Message sent when !link failes due to their account already being linked to an account.", "Your discord account is already linked to an account.");
	public static final MessageEntry M_LINK_SUCCESS = MessageEntry.create(LOCAL_ENTRIES, "command_link_success", "Message sent in the public channel when !link is run successfully.", "Your link key has been sent to you via private message.");
	public static final MessageEntry M_LINK_MESSAGE = MessageEntry.create(LOCAL_ENTRIES, "command_link_message", "Message sent in a PM to the player when !link is run successfully.\nMust contain the {linkkey} text.\n{linkkey} for the link key.\n{command} for the mincraft command suggestion '/link {linkkey}'", "Your link key is '{linkkey}'.\\nLog in to the server and run '{command}' to finish linking your account.","linkkey", "command");
	//!unlink
	public static final MessageEntry M_UNLINK_FAIL = MessageEntry.create(LOCAL_ENTRIES, "command_unlink_fail", "Message sent when !unlink fails to find a linked account for the player.", "Your discord account is not linked to a minecraft account on this server.");
	public static final MessageEntry M_UNLINK_SUCCESS = MessageEntry.create(LOCAL_ENTRIES, "command_unlink_success", "Message sent when !unlink successfully unlinks the player from their minecraft account.\n{player} for their minecraft name.", "Your discord account has been successfully unlinked from '{player}'.", "player");
	public static final MessageEntry M_UNLINK_PENDING = MessageEntry.create(LOCAL_ENTRIES, "command_unlink_success_pending", "Message sent when !unlink successfully unlinks the player from a pending link.", "You discord accounts pending link has been removed.");
	//!ign
	public static final MessageEntry M_IGN_SUCCESS = MessageEntry.create(LOCAL_ENTRIES, "command_ign_success", "Message sent when !ign is run successfully.", "{user} is linked to {player}", "user", "player");
	//!discordname
	public static final MessageEntry M_DISCORDNAME_SUCCESS = MessageEntry.create(LOCAL_ENTRIES, "command_discordname_success", "Message sent when !discordname is run successfully.\n{player} for their minecraft name.\n{user} for their discord name.", "'{player}' is linked to {member}", "player", "user");
	public static final MessageEntry M_DISCORDNAME_FAIL = MessageEntry.create(LOCAL_ENTRIES, "command_discordname_fail", "Message sent when !discordname could not find a linked account for the given player.\n{player} for their minecraft name.", "'{player}' is not linked to a discord account.", "player");
	
	//!help
	public static final MessageEntry M_HELP_HELP = MessageEntry.create(LOCAL_ENTRIES, "help_help", "Help message for !help.", "Show this help info.");
	public static final MessageEntry M_HELP_LINK = MessageEntry.create(LOCAL_ENTRIES, "help_link", "Help message for !link.", "Generate a link key to start the linking process.");
	public static final MessageEntry M_HELP_UNLINK = MessageEntry.create(LOCAL_ENTRIES, "help_unlink", "Help message for !unlink.", "Unlink your discord account from your minecraft username.");
	public static final MessageEntry M_HELP_LINKUSER = MessageEntry.create(LOCAL_ENTRIES, "help_linkuser", "Help message for !linkuser.", "Links the replied pinged users account to the given minecraft username.");
	public static final MessageEntry M_HELP_UNLINKPLAYER = MessageEntry.create(LOCAL_ENTRIES, "help_unlinkplayer", "Help message for !unlinkplayer.", "Unlinks the given minecraft user from their discord account.");
	public static final MessageEntry M_HELP_DISCORDLIST = MessageEntry.create(LOCAL_ENTRIES, "help_discordlist", "Help message for !discordlist.", "Lists data about every linked minecraft/discord account. **WARNING: SENSITIVE DATA, DO NOT RUN IN A PUBLIC CHANNEL!**");
	public static final MessageEntry M_HELP_DISCORDNAME = MessageEntry.create(LOCAL_ENTRIES, "help_discordname", "Help message for !discordname.", "Get the discord members name of the given minecraft account.");
	public static final MessageEntry M_HELP_IGN = MessageEntry.create(LOCAL_ENTRIES, "help_ign", "Help message for !ign.", "Get the ign of the pinged users minecraft account.");
	
	//Command outputs
	//'/linkdiscord'
	public static final MessageEntry M_COMMAND_LINK_COMPLETE = MessageEntry.create(LOCAL_ENTRIES, "command_linkdiscord_complete", "Message displayed when the player successfully runs the /linkdiscord [linkkey] command.\n{user} for their linked discord name.", "Your account has been successfully linked to your discord account.", "user");
	public static final MessageEntry M_COMMAND_LINK_BADKEY = MessageEntry.create(LOCAL_ENTRIES, "command_linkdiscord_badkey", "Message displayed when the player runs the /linkdiscord [linkkey] command with an invalid key.\n{linkkey} for the invalid link key.", "{linkkey} is not a valid link key.", "linkkey");
	public static final MessageEntry M_COMMAND_LINK_ALREADYLINKED = MessageEntry.create(LOCAL_ENTRIES, "command_linkdiscord_alreadylinked", "Message displayed when the player runs the /linkdiscord [linkkey] command when their minecraft account is already linked to a discord account.\n{user} for their linked discord name.", "Your account is already linked to a discord account.", "user");
	//'/unlinkdiscord'
	public static final MessageEntry M_COMMAND_UNLINK_COMPLETE = MessageEntry.create(LOCAL_ENTRIES, "command_unlinkdiscord_complete", "Message displayed when the player successfully runs the /unlinkdiscord command.\n{user} for their formerly linked discord name.", "You are no longer linked to {user}.", "user");
	public static final MessageEntry M_COMMAND_UNLINK_FAILED = MessageEntry.create(LOCAL_ENTRIES, "command_unlinkdiscord_fail", "Message displayed when the player runs the /unlinkdiscord command when they're not linked.", "Your account is not linked to a discord account.");
	//'telldiscord'
	public static final MessageEntry M_COMMAND_TELLDISCORD = MessageEntry.create(LOCAL_ENTRIES, "command_telldiscord", "Message displayed when an admin runs the /telldiscord <message> command successfully!", "Message '{message}' sent!", "message");

	public static void collectEntries()
	{
		LoadMessageEntriesEvent event = new LoadMessageEntriesEvent();
		ModList.get().forEachModContainer((id,c) -> {
			if(c instanceof FMLModContainer fmc)
				fmc.getEventBus().post(event);
		});
		ENTRIES = event.getEntries();
		LightmansDiscordIntegration.LOGGER.info(ENTRIES.size() + " message entries were registered.");
		reload();
	}

	public static void reload()
	{
		//Load the entries from the file
		readEntries();
		//Re-write the file to fix any missing entries, update comments, etc.
		writeFile();
	}

	@SubscribeEvent
	public static void registerIncludedMessages(LoadMessageEntriesEvent event) { event.register(LOCAL_ENTRIES); }
	
	private static List<String> readLines()
	{
		if(!MessageManager.MESSAGE_FILE.exists())
			return Lists.newArrayList();
		try {
			BufferedReader br = new BufferedReader(new FileReader(MessageManager.MESSAGE_FILE, StandardCharsets.UTF_8));
			List<String> lines = Lists.newArrayList();
			String line;
			while((line = br.readLine()) != null)
			{
				lines.add(line);
			}
			br.close();
			return lines;
		} catch(IOException e) {
			LightmansDiscordIntegration.LOGGER.error("Error reading message file.", e);
			return Lists.newArrayList();
		}
	}
	
	//Also adds 
	private static void readEntries()
	{
		//Read the lines from the file.
		List<String> lines = readLines();
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
					entry.loadCurrentValue(value);
					//Remove the entry from the missed entry list
					missedEntries.remove(entry);
				}
				else //Warn about the unexpected entry.
					LightmansDiscordIntegration.LOGGER.warn("Found unknown key '" + key + "' while loading the message file. Unexpected entry will be ignored.");
			}
		}
		
		//Reset the missed entries to their default values
		missedEntries.forEach(entry -> {
			entry.loadCurrentValue(entry.defaultValue);
			LightmansDiscordIntegration.LOGGER.warn("Message File was missing entry for '" + entry.key + "'. Resetting to default value.");
		});
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static void writeFile()
	{
		try {
			if(!MessageManager.MESSAGE_FILE.exists())
			{
				File folder = new File(MessageManager.MESSAGE_FILE.getParent());
				if(!folder.exists())
					folder.mkdirs();
				
				if(!MessageManager.MESSAGE_FILE.createNewFile())
				{
					LightmansDiscordIntegration.LOGGER.error("Unable to create the message file.");
					return;
				}
			}

			PrintWriter writer = new PrintWriter(MessageManager.MESSAGE_FILE, StandardCharsets.UTF_8);
			
			//Initial comments
			writer.println("#Message Inputs for Lightman's Discord Integration.");
			writer.println("#Type \\n for a new line. Some messages will have optional inputs surrounded by {} to be filled in with various data.");
			writer.println("#Each message should list the optional inputs, and what they'll be replaced by.");
			writer.println();

			for (MessageEntry entry : ENTRIES) {
				List<String> commentLines = Lists.newArrayList(entry.comment.split("\n"));
				commentLines.forEach(comment -> writer.println("#" + comment));
				writer.println(entry.key + "=" + entry.getCurrentValue());
				writer.println();
			}
			writer.close();
			
		} catch(IOException e) {
			LightmansDiscordIntegration.LOGGER.error("Error modifying the message file.", e);
		}
		
	}

	private static MessageEntry getEntry(@Nonnull String key) { return MessageEntry.getEntry(ENTRIES, key); }
	
}