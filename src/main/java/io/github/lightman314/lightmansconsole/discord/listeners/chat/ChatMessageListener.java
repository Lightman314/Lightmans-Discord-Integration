package io.github.lightman314.lightmansconsole.discord.listeners.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansconsole.LDIConfig;
import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.discord.listeners.types.SingleChannelListener;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import io.github.lightman314.lightmansconsole.message.MessageManager.MessageEntry;
import io.github.lightman314.lightmansconsole.util.MessageUtil;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ChatMessageListener extends SingleChannelListener {
	
	public enum ActivityType { DISABLED, LISTENING, PLAYING, WATCHING, COMPETING, STREAMING }
	
	private final MinecraftServer server;
	private final UUID senderID = new UUID(0,0);
	
	public ChatMessageListener(Supplier<String> channelID)
	{
		super(channelID, () -> LightmansDiscordIntegration.PROXY.getJDA());
		this.server = ServerLifecycleHooks.getCurrentServer();
		this.sendTextMessage(MessageManager.M_SERVER_BOOT.format());
		this.setTopic(MessageManager.M_TOPIC_BOOT.format());
		this.setActivityText(MessageManager.M_ACTIVITY_BOOT.format());
	}
	
	@Override
	public void onChannelMessageReceived(MessageReceivedEvent event)
	{
		if(event.getAuthor().isBot())
		{
			//If bot is allowed, send message to players
			if(LDIConfig.SERVER.chatBotWhitelist.get().contains(event.getAuthor().getId()))
			{
				ITextComponent message = formatDiscordMessage(MessageManager.M_FORMAT_MINECRAFT_BOT, event.getMember(), event.getMessage(), "bot");
				server.getPlayerList().getPlayers().forEach(player -> player.sendMessage(message, this.senderID));
				LightmansDiscordIntegration.LOGGER.info(message.getString());
			}
			return;
		}
		if(event.getMessage().getContentRaw().equals(LDIConfig.SERVER.listPlayerCommand.get()))
		{
			List<String> output = new ArrayList<>();
			List<ServerPlayerEntity> playerList = getPlayerList();
			output.add("There are " + getPlayerCount() + " players online.");
			String playerText = "";
			for(ServerPlayerEntity player : playerList)
			{
				if(playerText != "")
					playerText += ", ";
				playerText += player.getName().getString();
			}
			if(playerText.length() > 0)
				output.add(playerText);
			this.sendTextMessage(output);
			return;
		}
		ITextComponent message = formatDiscordMessage(MessageManager.M_FORMAT_MINECRAFT, event.getMember(), event.getMessage(), "user");
		server.getPlayerList().getPlayers().forEach(player -> player.sendMessage(message, this.senderID));
		LightmansDiscordIntegration.LOGGER.info(message.getString());
	}
	
	public void updatePlayerCount() { this.updatePlayerCount(false); }
	
	public void updatePlayerCount(boolean shrink)
	{
		//Update the channel topic
		this.setTopic(MessageManager.M_TOPIC_TEXT.format(this.getPlayerCount(shrink), this.getPlayerLimit()));
		//Update the bot's activity
		this.setActivityText(MessageManager.M_ACTIVITY_TEXT.format(this.getPlayerCount(shrink), this.getPlayerLimit()));
	}
	
	public void setActivityText(String text)
	{
		switch(LDIConfig.SERVER.botActivityType.get())
		{
		case LISTENING:
			this.getJDA().getPresence().setActivity(Activity.listening(text));
			break;
		case PLAYING:
			this.getJDA().getPresence().setActivity(Activity.playing(text));
			break;
		case WATCHING:
			this.getJDA().getPresence().setActivity(Activity.watching(text));
			break;
		case COMPETING:
			this.getJDA().getPresence().setActivity(Activity.competing(text));
			break;
		case STREAMING: //Special youtube link :P
			this.getJDA().getPresence().setActivity(Activity.streaming(text, LDIConfig.SERVER.botStreamURL.get()));
			break;
		default:
			//Do nothing if disabled
		}
	}
	
	private int getPlayerCount(boolean shrink)
	{
		return shrink ? getPlayerCount() - 1 : getPlayerCount();
	}
	
	private int getPlayerLimit()
	{
		return ServerLifecycleHooks.getCurrentServer().getMaxPlayers();
	}
	
	private int getPlayerCount()
	{
		return ServerLifecycleHooks.getCurrentServer().getCurrentPlayerCount();
	}
	
	private List<ServerPlayerEntity> getPlayerList()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server.getPlayerList().getPlayers();
	}
	
	@SubscribeEvent
	public void onServerMessage(ServerChatEvent event)
	{
		try {
			String message = MessageManager.M_FORMAT_DISCORD.format(event.getPlayer().getDisplayName().getString(), MessageUtil.formatMinecraftMessage(event.getMessage(), this.getGuild()));
			this.sendTextMessage(message);
		} catch(Exception e) { e.printStackTrace(); }
		
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		try {
			String playerName = event.getPlayer().getDisplayName().getString();
			this.sendTextMessage(MessageManager.M_PLAYER_JOIN.format(new TranslationTextComponent("multiplayer.player.joined", playerName).getString(), playerName));
			this.updatePlayerCount();
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
	{
		try { 
			String playerName = event.getPlayer().getDisplayName().getString();
			this.sendTextMessage(MessageManager.M_PLAYER_LEAVE.format(new TranslationTextComponent("multiplayer.player.left", playerName).getString(), playerName));
			//Tell it to shrink the count by 1 as the leaving player is *technically* still online at this point in time.
			this.updatePlayerCount(true);
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent event)
	{
		try {
			if(event.getEntity() instanceof PlayerEntity)
			{
				this.sendTextMessage(MessageManager.M_PLAYER_DEATH.format(event.getSource().getDeathMessage(event.getEntityLiving()), event.getEntityLiving().getDisplayName()));
			}
			else if(event.getEntityLiving().hasCustomName())
			{
				this.sendTextMessage(MessageManager.M_ENTITY_DEATH.format(event.getSource().getDeathMessage(event.getEntityLiving()), event.getEntityLiving().getDisplayName()));
			}
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	@SubscribeEvent
	public void onAchievementGet(AdvancementEvent ev)
	{
		try {
			if(ev.getAdvancement() != null && ev.getAdvancement().getDisplay() != null && ev.getAdvancement().getDisplay().shouldAnnounceToChat())
			{
				this.sendTextMessage(MessageManager.M_PLAYER_ACHIEVEMENT.format(ev.getPlayer().getDisplayName().getString(), ev.getAdvancement().getDisplay().getTitle().getString(), ev.getAdvancement().getDisplay().getDescription().getString()));
			}
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	@SubscribeEvent
	public void onServerReady(FMLServerStartedEvent event)
	{
		try {
			this.sendTextMessage(MessageManager.M_SERVER_READY.format());
			this.updatePlayerCount();
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	@SubscribeEvent
	public void onServerStop(FMLServerStoppingEvent event)
	{
		try {
			this.sendTextMessage(MessageManager.M_SERVER_STOP.format());
			this.setTopic(MessageManager.M_TOPIC_OFFLINE.format());
			this.setActivityText(MessageManager.M_ACTIVITY_OFFLINE.format());
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	public ITextComponent formatDiscordMessage(MessageEntry format, Member member, Message message, String userFormat)
	{
		String[] splitMessage = format.format(MessageUtil.formatMessageText(message, this.getGuild())).split("\\{" + userFormat + "\\}");
		IFormattableTextComponent result = new StringTextComponent(splitMessage[0]);
		for(int i = 1; i < splitMessage.length; ++i)
		{
			result.append(formatMemberName(member));
			result.appendString(splitMessage[i]);
		}
		return result;
		
	}
	
	public static ITextComponent formatMemberName(Member member)
	{
		return new StringTextComponent(member.getEffectiveName()).mergeStyle(Style.EMPTY
				.setColor(Color.fromInt(member.getColorRaw()))
				.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"<@!" + member.getId() + ">"))
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(MessageManager.M_MEMBER_HOVER.get())))
				);
	}
	
}
