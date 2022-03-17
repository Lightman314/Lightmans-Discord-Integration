package io.github.lightman314.lightmansconsole;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmansconsole.discord.listeners.chat.ChatMessageListener;
import io.github.lightman314.lightmansconsole.discord.listeners.chat.ChatMessageListener.ActivityType;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

	public static class Server
	{
		
		public final ForgeConfigSpec.ConfigValue<String> botToken;
		public final ForgeConfigSpec.EnumValue<ChatMessageListener.ActivityType> botActivityType;
		
		//Console Config
		public final ForgeConfigSpec.ConfigValue<String> consoleChannel;
		public final ForgeConfigSpec.ConfigValue<String> consoleCommandPrefix;
		//Chat Config
		public final ForgeConfigSpec.ConfigValue<String> chatChannel;
		public final ForgeConfigSpec.BooleanValue chatAllowPingEveryone;
		//Account Configs
		//public final ForgeConfigSpec.ConfigValue<String> accountChannel;
		//public final ForgeConfigSpec.ConfigValue<String> accountAdminChannel;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> accountAdminRole;
		public final ForgeConfigSpec.ConfigValue<String> accountCommandPrefix;
		public final ForgeConfigSpec.BooleanValue accountWhitelist;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> accountChannelBlacklist;
		
		//Currency Configs
		public final ForgeConfigSpec.ConfigValue<String> currencyChannel;
		public final ForgeConfigSpec.ConfigValue<String> currencyCommandPrefix;
		
		Server(ForgeConfigSpec.Builder builder)
		{
			
			builder.comment("Server configuration settings.").push("server");
			
			this.botToken = builder
					.comment("The discord bots token.")
					.define("token", "<EMPTY>");
			this.botActivityType = builder
					.comment("The bots activity type.")
					.defineEnum("activityType", ActivityType.STREAMING);
			
			//Chat Bot Settings
			builder.comment("Chat Formatting Settings").push("chat");
			
			this.chatChannel = builder
					.comment("The server_chat channel.")
					.define("channel", "000000000000000000");
			this.chatAllowPingEveryone = builder
					.comment("Whether minecraft players can ping @everyone in their chat messages.")
					.define("ping_everyone", false);
			
			builder.pop();
			
			//Console Bot Settings
			builder.comment("Console Bot Settings").push("console");
			
			this.consoleChannel = builder
					.comment("The console channel.")
					.define("channel", "000000000000000000");
			this.consoleCommandPrefix = builder
					.comment("The prefix required to execute console commands.")
					.define("prefix", "/");
			
			builder.pop();
			
			builder.comment("Account linking settings.").push("account");
			
			/*this.accountChannel = builder
					.comment("The account linking command channel.")
					.define("channel", "000000000000000000");
			this.accountAdminChannel = builder
					.comment("The channel where users with permission can run the 'linkuser' command.")
					.define("adminChannel", "000000000000000000");*/
			this.accountAdminRole = builder
					.comment("The role given to members that are allowed to run the 'linkuser' & 'unlinkplayer' command.")
					.defineList("adminRoles", ImmutableList.of("000000000000000000"), o -> o instanceof String);
			this.accountWhitelist = builder
					.comment("Whether a user should also be whitelisted when linked by the 'linkuser' command.")
					.define("autoWhitelist", false);
			this.accountCommandPrefix = builder
					.comment("Prefix for the account related commands.")
					.define("prefix", "!");
			this.accountChannelBlacklist = builder
					.comment("List of channel id's that the account bot should ignore commands from.")
					.defineList("channelBlacklist", new ArrayList<>(), o -> o instanceof String);
			
			builder.pop();
			
			builder.comment("Currency bot settings. Requires lightmanscurrency v1.0.5.0+ to use.").push("lightmanscurrency");
			
			this.currencyChannel = builder
					.comment("The channel where users can run the currency commands and where currency related announcements will be made.")
					.define("channel", "000000000000000000");
			this.currencyCommandPrefix = builder
					.comment("Prefix for currency commands.")
					.define("prefix", "!");
			
			builder.pop();
			
		}
		
	}
	
	public static final ForgeConfigSpec serverSpec;
	public static final Config.Server SERVER;
	
	static
	{
		//Server
		final Pair<Server,ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Config.Server::new);
		serverSpec = serverPair.getRight();
		SERVER = serverPair.getLeft();
	}
	
}
