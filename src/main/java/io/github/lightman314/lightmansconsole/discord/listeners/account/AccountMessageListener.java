package io.github.lightman314.lightmansconsole.discord.listeners.account;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansconsole.Config;
import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.commands.CommandDiscordLink;
import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.links.PendingLink;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import io.github.lightman314.lightmansconsole.util.MemberUtil;
import io.github.lightman314.lightmansconsole.util.MessageUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber
public class AccountMessageListener extends ListenerAdapter implements ICommandSource{
	
	private final List<AccountCommand> REGISTERED_COMMANDS = new ArrayList<>();
	
	static List<String> commandOutput = new ArrayList<>();
	MinecraftServer server;
	static CommandSource commandSource;
	
	Supplier<JDA> jdaSource;
	public JDA getJDA() { return this.jdaSource.get(); }
	Supplier<String> prefixSource = () -> Config.SERVER.accountCommandPrefix.get();
	
	public AccountMessageListener()
	{
		this.jdaSource = () -> LightmansDiscordIntegration.PROXY.getJDA();
		this.server = ServerLifecycleHooks.getCurrentServer();
		commandSource = getCommandSource();
		MinecraftForge.EVENT_BUS.post(new RegisterAccountCommandEvent(this));
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if(event.getAuthor().isBot())
			return;
		
		boolean isAdmin = isAdmin(event.getMember());
		
		List<? extends String> blacklistedChannels = Config.SERVER.accountChannelBlacklist.get();
		for(int i = 0; i < blacklistedChannels.size(); i++)
		{
			if(blacklistedChannels.get(i).equals(event.getChannel().getId()))
				return;
		}
		
		//Run command
		String input = event.getMessage().getContentRaw();
		String prefix = Config.SERVER.accountCommandPrefix.get();
		if(input.startsWith(prefix))
		{
			final String command = input.substring(prefix.length());
			if(command.startsWith("linkuser"))
			{
				if(!isAdmin)
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_ERROR_PERMISSIONS.format());
					return;
				}
				String subcommand = command.substring(8);
				LightmansDiscordIntegration.LOGGER.info(command + " -> " + subcommand);
				Member linkingUser = MemberUtil.getMemberFromPing(event.getGuild(), subcommand);
				String playerName = "";
				int endIndex = subcommand.indexOf('>');
				if(endIndex >= 0 & endIndex < subcommand.length())
					playerName = subcommand.substring(endIndex + 1).replace(" ", ""); //Wipe empty space from the name
				else
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_ERROR_NOPING.format());
					return;
				}
				if(linkingUser != null)
				{
					List<String> output = AccountManager.tryLinkUser2(linkingUser.getUser(), playerName);
					MessageUtil.sendTextMessage(event.getTextChannel(), output);
					if(Config.SERVER.accountWhitelist.get())
					{
						MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
						server.getCommandManager().handleCommand(commandSource, "whitelist add " + playerName);
						MessageUtil.sendTextMessage(event.getTextChannel(), commandOutput);
						commandOutput.clear();
					}
					MessageUtil.sendPrivateMessage(linkingUser.getUser(), MessageManager.M_LINKUSER_WELCOME.format());
				}
				else
				{
					MessageUtil.sendTextMessage(event.getTextChannel(), MessageManager.M_ERROR_PING.format());
				}
			}
			else if(command.startsWith("unlinkplayer "))
			{
				if(!isAdmin)
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_ERROR_PERMISSIONS.format());
					return;
				}
				String playerName = command.substring(13);
				List<String> output = AccountManager.tryForceUnlinkUser(event.getJDA(), playerName);
				MessageUtil.sendTextMessage(event.getTextChannel(), output);
			}
			else if(command.startsWith("link"))
			{
				PendingLink pendingLink = AccountManager.createPendingLink(event.getAuthor());
				if(pendingLink == null)
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_LINK_FAIL.format());
				else //Send PM to user with their link key.
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_LINK_SUCCESS.format());
					MessageUtil.sendPrivateMessage(event.getAuthor(), MessageManager.M_LINK_MESSAGE.format(pendingLink.linkKey, "/" + CommandDiscordLink.COMMAND_LITERAL + " " + pendingLink.linkKey));
				}
			}
			else if(command.startsWith("unlink"))
			{
				List<String> output = new ArrayList<>();
				output = AccountManager.tryUnlinkUser(event.getAuthor());
				MessageUtil.sendTextMessage(event.getChannel(), output);
			}
			else if(command.startsWith("discordlist"))
			{
				if(!isAdmin)
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_ERROR_PERMISSIONS.format());
					return;
				}
				List<String> output = new ArrayList<>();
				output.add("------**Linked Accounts**------");
				AccountManager.getLinkedAccounts().forEach(la ->{
					output.add("DiscordID: " + la.discordID);
					Member member = event.getGuild().getMemberById(la.discordID);
					if(member != null)
						output.add("Discord Name: " + member.getEffectiveName());
					output.add("Minecraft ID: " + la.playerID.toString());
					output.add("Minecraft Name: " + la.getName());
					output.add("");
				});
				output.add("------**Pending Links**------");
				AccountManager.getPendingLinks().forEach(pl ->{
					output.add("DiscordID: " + pl.userID);
					Member member = event.getGuild().getMemberById(pl.userID);
					if(member != null)
						output.add("Discord Name: " + member.getEffectiveName());
					output.add("Link Key: " + pl.linkKey);
				});
				MessageUtil.sendTextMessage(event.getChannel(), output);
			}
			else if(command.startsWith("help"))
			{
				String commandPrefix = Config.SERVER.accountCommandPrefix.get();
				List<String> output = new ArrayList<>();
				output.add("Minecraft-Discord Account Linkage Help:");
				output.add(commandPrefix + "help - " + MessageManager.M_HELP_HELP.format());
				output.add(commandPrefix + "link - " + MessageManager.M_HELP_LINK.format());
				output.add(commandPrefix + "unlink - " + MessageManager.M_HELP_UNLINK.format());
				if(isAdmin)
				{
					output.add(commandPrefix + "linkuser @user <MINECRAFT_USERNAME> - " + MessageManager.M_HELP_LINKUSER.format());
					output.add(commandPrefix + "unlinkplayer <MINECRAFT_USERNAME> - " + MessageManager.M_HELP_UNLINKPLAYER.format());
					output.add(commandPrefix + "discordlist - " + MessageManager.M_HELP_DISCORDLIST.format());
				}
				this.REGISTERED_COMMANDS.forEach(c -> {
					if(c.canRun(isAdmin))
						c.addToHelpText(output, commandPrefix);
				});
				MessageUtil.sendTextMessage(event.getChannel(), output);
			}else
			{
				List<String> output = new ArrayList<>();
				//Get the linked account for this user
				LinkedAccount account = AccountManager.getLinkedAccountFromUser(event.getAuthor());
				REGISTERED_COMMANDS.forEach(c ->{
					if(command.startsWith(c.literal) && c.canRun(isAdmin))
					{
						try { //Catch any unexpected errors
							c.runCommand(command, account, event.getGuild(), output);
						} catch(Exception e) { e.printStackTrace(); }
					}
						
				});
				//Output the given text
				MessageUtil.sendTextMessage(event.getChannel(), output);
			}
		}
	}
	
	public static boolean isAdmin(Member member)
	{
		List<Role> roles = member.getRoles();
		for(int i = 0; i < roles.size(); i++)
		{
			if(Config.SERVER.accountAdminRole.get().contains(roles.get(i).getId()))
				return true;
		}
		return false;
	}
	
	private CommandSource getCommandSource()
	{
		ServerWorld world = server.func_241755_D_();
		return new CommandSource(this, world == null ? Vector3d.ZERO : Vector3d.copy(world.getSpawnPoint()), Vector2f.ZERO, world, 4, "AccountBot", new StringTextComponent("AccountBot"), server, (Entity)null);
	}
	
	public void registerCommand(AccountCommand command)
	{
		if(!this.REGISTERED_COMMANDS.contains(command))
			this.REGISTERED_COMMANDS.add(command);
	}
	
	public boolean allowLogging() { return true; }
	public boolean shouldReceiveErrors() { return true; }
	public boolean shouldReceiveFeedback() { return true; }
	public void sendMessage(ITextComponent component, UUID sender)
	{
		commandOutput.add(component.getString());
	}
	
	public static class RegisterAccountCommandEvent extends Event
	{
		
		private final AccountMessageListener listener;
		
		public RegisterAccountCommandEvent(AccountMessageListener listener)
		{
			this.listener = listener;
		}
		
		public void registerCommand(AccountCommand command)
		{
			this.listener.registerCommand(command);
			command.setup(this.listener.jdaSource, this.listener.prefixSource);
		}
		
	}
	
}
