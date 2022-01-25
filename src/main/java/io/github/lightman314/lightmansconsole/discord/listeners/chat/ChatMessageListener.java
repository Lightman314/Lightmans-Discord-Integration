package io.github.lightman314.lightmansconsole.discord.listeners.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansconsole.Config;
import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.discord.listeners.types.SingleChannelListener;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import io.github.lightman314.lightmansconsole.util.MessageUtil;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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

public class ChatMessageListener extends SingleChannelListener {
	
	public enum ActivityType { DISABLED, LISTENING, PLAYING, WATCHING, COMPETING, STREAMING }
	
	private static ChatMessageListener instance = null;
	private final MinecraftServer server;
	private final UUID senderID = new UUID(0,0);
	
	public ChatMessageListener(Supplier<String> channelID)
	{
		super(channelID, () -> LightmansDiscordIntegration.PROXY.getJDA());
		instance = this;
		this.server = ServerLifecycleHooks.getCurrentServer();
		this.sendTextMessage(MessageManager.M_SERVER_BOOT.get());
		this.setTopic(MessageManager.M_TOPIC_BOOT.get());
		this.setActivityText(MessageManager.M_ACTIVITY_BOOT.get());
	}
	
	@Override
	public void onChannelMessageReceived(MessageReceivedEvent event)
	{
		if(event.getAuthor().isBot())
			return;
		if(event.getMessage().getContentRaw().startsWith("/list"))
		{
			List<String> output = new ArrayList<>();
			List<ServerPlayer> playerList = getPlayerList();
			output.add("There are " + getPlayerCount() + " players online.");
			String playerText = "";
			for(ServerPlayer player : playerList)
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
		Component message = formatDiscordMessage(event.getMember(), event.getMessage());
		server.getPlayerList().getPlayers().forEach(player -> player.sendMessage(message, this.senderID));
		LightmansDiscordIntegration.LOGGER.info(message.getString());
	}
	
	public void updatePlayerCount() { this.updatePlayerCount(false); }
	
	public void updatePlayerCount(boolean shrink)
	{
		//Update the channel topic
		this.setTopic(MessageManager.M_TOPIC_TEXT.format(this.getPlayerCount(shrink)));
		//Update the bot's activity
		this.setActivityText(MessageManager.M_ACTIVITY_TEXT.format(this.getPlayerCount(shrink), this.getPlayerLimit()));
	}
	
	public void setActivityText(String text)
	{
		switch(Config.SERVER.botActivityType.get())
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
			this.getJDA().getPresence().setActivity(Activity.streaming(text, "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
			break;
		default:
			//Do nothing if disabled
		}
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
		return ServerLifecycleHooks.getCurrentServer().getPlayerCount();
	}
	
	private List<ServerPlayer> getPlayerList()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server.getPlayerList().getPlayers();
	}
	
	@SubscribeEvent
	public void onServerMessage(ServerChatEvent event)
	{
		String message = MessageManager.M_FORMAT_DISCORD.format(event.getPlayer().getDisplayName().getString(), MessageUtil.formatMinecraftMessage(event.getMessage(), this.getGuild()));
		if(instance != null)
			instance.sendTextMessage(message);
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(instance != null)
		{
			Component playerName = event.getPlayer().getDisplayName();
			instance.sendTextMessage(MessageManager.M_PLAYER_JOIN.format(new TranslatableComponent("multiplayer.player.joined", playerName), playerName));
			instance.updatePlayerCount();
		}
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
	{
		if(instance != null)
		{
			Component playerName = event.getPlayer().getDisplayName();
			instance.sendTextMessage(MessageManager.M_PLAYER_LEAVE.format(new TranslatableComponent("multiplayer.player.left", playerName), playerName));
			//Tell it to shrink the count by 1 as the leaving player is *technically* still online at this point in time.
			instance.updatePlayerCount(true);
		}
	}
	
	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event)
	{
		if(event.getEntity() instanceof Player && instance != null)
		{
			instance.sendTextMessage(event.getSource().getLocalizedDeathMessage(event.getEntityLiving()).getString());
		}
	}
	
	@SubscribeEvent
	public void onAchievementGet(AdvancementEvent ev)
	{
		if(instance != null && ev.getAdvancement() != null && ev.getAdvancement().getDisplay() != null && ev.getAdvancement().getDisplay().shouldAnnounceChat())
		{
			instance.sendTextMessage(MessageManager.M_PLAYER_ACHIEVEMENT.format(ev.getPlayer().getDisplayName(), ev.getAdvancement().getDisplay().getTitle(), ev.getAdvancement().getDisplay().getDescription()));
		}
	}
	
	@SubscribeEvent
	public void onServerReady(ServerStartedEvent event)
	{
		this.sendTextMessage(MessageManager.M_SERVER_READY.get());
		this.updatePlayerCount();
	}
	
	@SubscribeEvent
	public void onServerStop(ServerStoppingEvent event)
	{
		this.sendTextMessage(MessageManager.M_SERVER_STOP.get());
		this.setTopic(MessageManager.M_TOPIC_OFFLINE.get());
		this.setActivityText(MessageManager.M_ACTIVITY_OFFLINE.get());
	}
	
	public Component formatDiscordMessage(Member member, Message message)
	{
		return new TextComponent(MessageManager.M_FORMAT_MINECRAFT_PREFIX.get())
				.append(formatMemberName(member))
				.append(MessageManager.M_FORMAT_MINECRAFT_POSTFIX.get())
				.append(" ")
				.append(MessageUtil.formatMessageText(message, this.getGuild()));
	}
	
	public static Component formatMemberName(Member member)
	{
		return new TextComponent(member.getEffectiveName()).withStyle(Style.EMPTY
				.withColor(TextColor.fromRgb(member.getColorRaw()))
				.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"<@!" + member.getId() + ">"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Mention")))
				);
	}
	
}
