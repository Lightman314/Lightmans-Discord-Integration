package io.github.lightman314.lightmansdiscord;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmansdiscord.api.jda.JDAUtil;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraftforge.common.ForgeConfigSpec;

public class LDIConfig {

	public static class Server
	{
		
		public final ForgeConfigSpec.ConfigValue<String> botToken;
		public final ForgeConfigSpec.EnumValue<JDAUtil.ActivityType> botActivityType;
		public final ForgeConfigSpec.ConfigValue<String> botStreamURL;
		
		//Console Config
		public final ForgeConfigSpec.ConfigValue<String> consoleChannel;
		public final ForgeConfigSpec.ConfigValue<String> consoleCommandPrefix;
		//Chat Config
		public final ForgeConfigSpec.ConfigValue<String> chatChannel;
		public final ForgeConfigSpec.BooleanValue chatAllowPingEveryone;
		public final ForgeConfigSpec.BooleanValue postEntityDeaths;
		public final ForgeConfigSpec.ConfigValue<String> listPlayerCommand;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> chatBotWhitelist;
		//Account Configs
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> accountAdminRole;
		public final ForgeConfigSpec.ConfigValue<String> accountCommandPrefix;
		public final ForgeConfigSpec.BooleanValue accountWhitelist;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> accountChannelBlacklist;
		
		Server(ForgeConfigSpec.Builder builder)
		{
			
			builder.comment("Server configuration settings.").push("server");
			
			this.botToken = builder
					.comment("The discord bots token.")
					.define("token", "<EMPTY>");
			this.botActivityType = builder
					.comment("The bots activity type.")
					.defineEnum("activityType", JDAUtil.ActivityType.STREAMING);
			this.botStreamURL = builder
					.comment("The streaming URL if activity type is set to streaming.")
					.define("streamingURL", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
			
			//Chat Bot Settings
			builder.comment("Chat Formatting Settings").push("chat");
			
			this.chatChannel = builder
					.comment("The server_chat channel.")
					.define("channel", "000000000000000000");
			this.chatAllowPingEveryone = builder
					.comment("Whether minecraft players can ping @everyone in their chat messages.")
					.define("ping_everyone", false);
			this.postEntityDeaths = builder
					.comment("Whether the deaths of named entities should be posted in server chat. Disable if there are mods that spawn entities with custom names.")
					.define("postEntityDeaths", true);
			this.listPlayerCommand = builder
					.comment("The command that can be run in the server chat channel to list all online players.")
					.define("listPlayerCommand", "/list");
			this.chatBotWhitelist = builder
					.comment("List of bot id's whos messages will be transmitted to minecraft chat when a message is sent in the server chat channel.")
					.defineList("botWhitelist", new ArrayList<>(), o -> o instanceof String);
			
			builder.pop();
			
			//Console Bot Settings
			builder.comment("Console Bot Settings").push("console");
			
			this.consoleChannel = builder
					.comment("The console channel.",
							"IMPORTANT NOTE: This channel allows any discord user to run admin commands without prejudice, so make sure that this channel is only accessible by those you trust.")
					.define("channel", "000000000000000000");
			this.consoleCommandPrefix = builder
					.comment("The prefix required to execute console commands.",
							"Leave empty to have all text inputs be processed as a command.")
					.define("prefix", "/");
			
			builder.pop();
			
			builder.comment("Account linking settings.").push("account");

			this.accountAdminRole = builder
					.comment("The role given to members that are allowed to run the 'linkuser' & 'unlinkplayer' command.")
					.defineList("adminRoles", Lists.newArrayList("000000000000000000"), o -> o instanceof String);
			this.accountWhitelist = builder
					.comment("Whether a user should also be whitelisted when linked by the 'linkuser' command.")
					.define("autoWhitelist", false);
			this.accountCommandPrefix = builder
					.comment("Prefix for the account related commands.")
					.define("prefix", "!");
			this.accountChannelBlacklist = builder
					.comment("List of channel id's that the account bot should ignore commands from.",
							"Note: Channels used by other listeners will be automatically ignored.")
					.defineList("channelBlacklist", new ArrayList<>(), o -> o instanceof String);
			
			builder.pop();
			
		}
		
	}
	
	public static final ForgeConfigSpec serverSpec;
	public static final LDIConfig.Server SERVER;
	
	static
	{
		//Server
		final Pair<Server,ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(LDIConfig.Server::new);
		serverSpec = serverPair.getRight();
		SERVER = serverPair.getLeft();
	}
	
}
