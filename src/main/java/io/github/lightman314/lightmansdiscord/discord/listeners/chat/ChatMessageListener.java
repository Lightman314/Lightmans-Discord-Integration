package io.github.lightman314.lightmansdiscord.discord.listeners.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansdiscord.LDIConfig;
import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import io.github.lightman314.lightmansdiscord.api.jda.JDAUtil;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeMemberReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.messages.SafeMessageReference;
import io.github.lightman314.lightmansdiscord.api.jda.listeners.SafeSingleChannelListener;
import io.github.lightman314.lightmansdiscord.compat.PlayerVisibilityUtil;
import io.github.lightman314.lightmansdiscord.message.MessageEntry;
import io.github.lightman314.lightmansdiscord.message.MessageManager;
import io.github.lightman314.lightmansdiscord.util.MessageUtil;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ChatMessageListener extends SafeSingleChannelListener {
	
	private static final UUID SYSTEM_ID = new UUID(0,0);
	
	private static ChatMessageListener instance = null;
	
	private final MinecraftServer server;
	
	public ChatMessageListener(Supplier<String> channelID)
	{
		super(channelID);
		instance = this;
		this.server = ServerLifecycleHooks.getCurrentServer();
		this.sendMessage(MessageManager.M_SERVER_BOOT.get());
		this.setTopic(MessageManager.M_TOPIC_BOOT.get());
		this.setActivityText(MessageManager.M_ACTIVITY_BOOT.get());
	}

	@Override
	protected void OnTextChannelMessage(SafeMemberReference member, SafeMessageReference message) {

		if(member.isBot())
		{
			//If bot is allowed, send message to players
			if(LDIConfig.SERVER.chatBotWhitelist.get().contains(member.getID()))
			{
				Component output = formatDiscordMessage(MessageManager.M_FORMAT_MINECRAFT_BOT, member, message, "bot");
				server.getPlayerList().getPlayers().forEach(player -> player.sendMessage(output,SYSTEM_ID));
				LightmansDiscordIntegration.LOGGER.info(output.getString());
			}
			return;
		}
		if(message.getRaw().equals(LDIConfig.SERVER.listPlayerCommand.get()))
		{
			List<String> output = new ArrayList<>();
			List<ServerPlayer> playerList = PlayerVisibilityUtil.getPlayerList();
			output.add("There are " + playerList.size() + " players online.");
			StringBuilder playerText = new StringBuilder();
			for(ServerPlayer player : playerList)
			{
				if(!playerText.toString().equals(""))
					playerText.append(", ");
				playerText.append(player.getName().getString());
			}
			if(playerText.length() > 0)
				output.add(playerText.toString());
			this.sendMessage(output);
			return;
		}
		Component output = formatDiscordMessage(MessageManager.M_FORMAT_MINECRAFT, member, message, "user");
		server.getPlayerList().getPlayers().forEach(player -> player.sendMessage(output,SYSTEM_ID));
		LightmansDiscordIntegration.LOGGER.info(output.getString());
	}
	
	public void updatePlayerCount() { this.updatePlayerCount(false); }
	
	public void updatePlayerCount(boolean shrink)
	{
		int playerCount = this.getPlayerCount(shrink);
		//Update the channel topic
		this.setTopic(MessageManager.M_TOPIC_TEXT.format(playerCount));
		//Update the bots activity
		this.setActivityText(MessageManager.M_ACTIVITY_TEXT.format(playerCount, this.getPlayerLimit()));
	}
	
	public final void setActivityText(String text)
	{
		JDAUtil.SetActivity(LDIConfig.SERVER.botActivityType.get(), text, LDIConfig.SERVER.botStreamURL.get());
	}
	
	private int getPlayerCount(boolean shrink)
	{
		return shrink ? this.getPlayerCount() - 1 : this.getPlayerCount();
	}
	
	private int getPlayerLimit()
	{
		return ServerLifecycleHooks.getCurrentServer().getMaxPlayers();
	}
	
	private int getPlayerCount()
	{
		return PlayerVisibilityUtil.getPlayerList().size();
	}
	
	@SubscribeEvent
	public void onServerMessage(ServerChatEvent event)
	{
		try {
			String message = MessageManager.M_FORMAT_DISCORD.format(MessageUtil.clearFormatting(event.getPlayer().getDisplayName().getString()), MessageUtil.formatMinecraftMessage(event.getMessage(), this.getGuild()));
			this.sendMessage(message);
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(PlayerVisibilityUtil.isPlayerVisible(event.getPlayer()))
			fakePlayerJoin(event.getPlayer());
	}
	
	public static void fakePlayerJoin(Player player) {
		try {
			if(instance != null)
			{
				Component playerName = player.getDisplayName();
				instance.sendMessage(MessageManager.M_PLAYER_JOIN.format(new TranslatableComponent("multiplayer.player.joined", playerName), playerName));
				instance.updatePlayerCount();
			}
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
	{
		if(PlayerVisibilityUtil.isPlayerVisible(event.getPlayer()))
			fakePlayerLeave(event.getPlayer(), true);
	}
	
	public static void fakePlayerLeave(Player player, boolean shrinkPlayerCount) {
		try {
			if(instance != null)
			{
				Component playerName = player.getDisplayName();
				instance.sendMessage(MessageManager.M_PLAYER_LEAVE.format(new TranslatableComponent("multiplayer.player.left", playerName), playerName));
				//Tell it to shrink the count by 1 as the leaving player is *technically* still online at this point in time.
				instance.updatePlayerCount(shrinkPlayerCount);
			}
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent event)
	{
		try {
			if(event.getEntity() instanceof Player)
			{
				this.sendMessage(MessageManager.M_PLAYER_DEATH.format(event.getSource().getLocalizedDeathMessage(event.getEntityLiving()), event.getEntity().getDisplayName()));
			}
			else if(event.getEntity().hasCustomName() && LDIConfig.SERVER.postEntityDeaths.get())
			{
				this.sendMessage(MessageManager.M_ENTITY_DEATH.format(event.getSource().getLocalizedDeathMessage(event.getEntityLiving()), event.getEntity().getDisplayName()));
			}
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	@SubscribeEvent
	public void onAchievementGet(AdvancementEvent ev)
	{
		try {
			if(ev.getAdvancement() != null && ev.getAdvancement().getDisplay() != null && ev.getAdvancement().getDisplay().shouldAnnounceChat() && PlayerVisibilityUtil.isPlayerVisible(ev.getPlayer()))
			{
				this.sendMessage(MessageManager.M_PLAYER_ACHIEVEMENT.format(ev.getEntity().getDisplayName(), ev.getAdvancement().getDisplay().getTitle(), ev.getAdvancement().getDisplay().getDescription()));
			}
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	@SubscribeEvent
	public void onServerReady(ServerStartedEvent event)
	{
		try {
			this.sendMessage(MessageManager.M_SERVER_READY.get());
			this.updatePlayerCount();
		} catch(Exception e) { e.printStackTrace(); }
		
	}
	
	@SubscribeEvent
	public void onServerStop(ServerStoppingEvent event)
	{
		try {
			this.sendMessage(MessageManager.M_SERVER_STOP.get());
			this.setTopic(MessageManager.M_TOPIC_OFFLINE.get());
			this.setActivityText(MessageManager.M_ACTIVITY_OFFLINE.get());
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	public static boolean isPresent() {
		return instance != null;
	}
	
	public static void sendChatMessage(String message) 
	{
		if(instance != null)
			instance.sendMessage(message);
	}
	
	public Component formatDiscordMessage(MessageEntry format, SafeMemberReference member, SafeMessageReference message, String userFormat)
	{
		String[] splitMessage = format.format(MessageUtil.formatMessageText(message, this.getGuild())).split("\\{" + userFormat + "\\}");
		MutableComponent result = new TextComponent(splitMessage[0]);
		for(int i = 1; i < splitMessage.length; ++i)
		{
			result.append(formatMemberName(member));
			result.append(splitMessage[i]);
		}
		return result;
	}
	
	public static Component formatMemberName(SafeMemberReference member)
	{
		return new TextComponent(member.getEffectiveName()).withStyle(Style.EMPTY
				.withColor(TextColor.fromRgb(member.getColor()))
				.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"<@!" + member.getID() + ">"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent((MessageManager.M_MEMBER_HOVER.get())))
				));
	}
	
}
