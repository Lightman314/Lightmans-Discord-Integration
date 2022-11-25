package io.github.lightman314.lightmansconsole.discord.listeners.account;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmansconsole.LDIConfig;
import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.commands.CommandDiscordLink;
import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.links.PendingLink;
import io.github.lightman314.lightmansconsole.discord.listeners.types.MultiChannelListener;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import io.github.lightman314.lightmansconsole.util.MemberUtil;
import io.github.lightman314.lightmansconsole.util.MessageUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber
public class AccountMessageListener extends ListenerAdapter implements CommandSource{
	
	private final List<AccountCommand> REGISTERED_COMMANDS = new ArrayList<>();
	
	private final List<String> commandOutput = new ArrayList<>();
	MinecraftServer server;
	
	public AccountMessageListener()
	{
		this.server = ServerLifecycleHooks.getCurrentServer();
		MinecraftForge.EVENT_BUS.post(new RegisterAccountCommandEvent(this));
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if(event.getAuthor().isBot())
			return;
		
		boolean isAdmin = isAdmin(event.getMember());
		
		if(!MultiChannelListener.canListenToChannel(LDIConfig.SERVER.accountChannelBlacklist.get(), event.getChannel().getId()))
			return;
		
		//Run command
		String input = event.getMessage().getContentRaw();
		String prefix = LDIConfig.SERVER.accountCommandPrefix.get();
		if(input.startsWith(prefix))
		{
			final String command = input.substring(prefix.length());
			if(command.startsWith("linkuser"))
			{
				if(!isAdmin)
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_ERROR_PERMISSIONS.get());
					return;
				}
				String subcommand = command.substring(8);
				LightmansDiscordIntegration.LOGGER.info(command + " -> " + subcommand);
				Member linkingUser = MemberUtil.getMemberFromPing(event.getGuild(), subcommand);
				String playerName;
				int endIndex = subcommand.indexOf('>');
				if(endIndex >= 0)
					playerName = subcommand.substring(endIndex + 1).replace(" ", ""); //Wipe empty space from the name
				else
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_ERROR_NOPING.get());
					return;
				}
				if(linkingUser != null)
				{
					List<String> output = new ArrayList<>();
					try {
						output.addAll(AccountManager.tryLinkUser2(linkingUser.getUser(), playerName));
					} catch(Exception e) { e.printStackTrace(); }
					if(LDIConfig.SERVER.accountWhitelist.get())
					{
						try {
							MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
							server.getCommands().performPrefixedCommand(this.getCommandSource(), "whitelist add " + playerName);
							output.addAll(this.commandOutput);
							this.commandOutput.clear();
						} catch(Exception e) { e.printStackTrace(); }
					}
					MessageUtil.sendTextMessage(event.getChannel(), output);
					MessageUtil.sendPrivateMessage(linkingUser.getUser(), MessageManager.M_LINKUSER_WELCOME.get());
				}
				else
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_ERROR_PING.get());
				}
			}
			else if(command.startsWith("unlinkplayer "))
			{
				if(!isAdmin)
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_ERROR_PERMISSIONS.get());
					return;
				}
				String playerName = command.substring(13);
				List<String> output = AccountManager.tryForceUnlinkUser(playerName);
				MessageUtil.sendTextMessage(event.getChannel(), output);
			}
			else if(command.startsWith("link"))
			{
				PendingLink pendingLink = AccountManager.createPendingLink(event.getAuthor());
				if(pendingLink == null)
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_LINK_FAIL.get());
				else //Send PM to user with their link key.
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_LINK_SUCCESS.get());
					MessageUtil.sendPrivateMessage(event.getAuthor(), MessageManager.M_LINK_MESSAGE.format(pendingLink.linkKey, "/" + CommandDiscordLink.COMMAND_LITERAL + " " + pendingLink.linkKey));
				}
			}
			else if(command.startsWith("unlink"))
			{
				MessageUtil.sendTextMessage(event.getChannel(), AccountManager.tryUnlinkUser(event.getAuthor()));
			}
			else if(command.startsWith("discordlist"))
			{
				if(!isAdmin)
				{
					MessageUtil.sendTextMessage(event.getChannel(), MessageManager.M_ERROR_PERMISSIONS.get());
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
				String commandPrefix = LDIConfig.SERVER.accountCommandPrefix.get();
				List<String> output = new ArrayList<>();
				output.add("Minecraft-Discord Account Linkage Help:");
				output.add(commandPrefix + "help - " + MessageManager.M_HELP_HELP.get());
				output.add(commandPrefix + "link - " + MessageManager.M_HELP_LINK.get());
				output.add(commandPrefix + "unlink - " + MessageManager.M_HELP_UNLINK.get());
				if(isAdmin)
				{
					output.add(commandPrefix + "linkuser @user <MINECRAFT_USERNAME> - " + MessageManager.M_HELP_LINKUSER.get());
					output.add(commandPrefix + "unlinkplayer <MINECRAFT_USERNAME> - " + MessageManager.M_HELP_UNLINKPLAYER.get());
					output.add(commandPrefix + "discordlist - " + MessageManager.M_HELP_DISCORDLIST.get());
				}
				this.REGISTERED_COMMANDS.forEach(c -> {
					if(c.canRun(isAdmin))
						c.addToHelpText(output, commandPrefix);
				});
				MessageUtil.sendTextMessage(event.getChannel(), output);
			}else
			{
				final List<String> output = new ArrayList<>();
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
		if(member == null)
			return false;
		List<Role> roles = member.getRoles();
		for (Role role : roles) {
			if (LDIConfig.SERVER.accountAdminRole.get().contains(role.getId()))
				return true;
		}
		return false;
	}
	
	private CommandSourceStack getCommandSource()
	{
		ServerLevel level = this.server.overworld();
		return new CommandSourceStack(this, Vec3.atBottomCenterOf(level.getSharedSpawnPos()), Vec2.ZERO, level, 4, "AccountBot", Component.literal("AccountBot"), server, null);
	}
	
	public void registerCommand(AccountCommand command)
	{
		if(!this.REGISTERED_COMMANDS.contains(command))
			this.REGISTERED_COMMANDS.add(command);
	}
	
	public boolean shouldInformAdmins() { return true; }
	public boolean acceptsFailure() { return true; }
	public boolean acceptsSuccess() { return true; }
	public void sendSystemMessage(Component component)
	{
		this.commandOutput.add(component.getString());
	}
	
	public static class RegisterAccountCommandEvent extends Event
	{
		
		private final AccountMessageListener listener;
		
		public RegisterAccountCommandEvent(AccountMessageListener listener)
		{
			this.listener = listener;
		}
		
		public void registerCommand(AccountCommand command) {
			this.listener.registerCommand(command);
		}
		
	}
	
}
