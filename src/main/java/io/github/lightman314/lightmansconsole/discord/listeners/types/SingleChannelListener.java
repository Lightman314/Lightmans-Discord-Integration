package io.github.lightman314.lightmansconsole.discord.listeners.types;

import java.util.List;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansconsole.util.MessageUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class SingleChannelListener extends ListenerAdapter{

	private final Supplier<String> channelID;
	private final Supplier<JDA> jdaSource;
	
	protected SingleChannelListener(Supplier<String> channel, Supplier<JDA> jdaSource)
	{
		this.channelID = channel;
		this.jdaSource = jdaSource;
		if(this.blockMultiChannelListeners())
			MultiChannelListener.ignoreChannel(this.channelID);
	}
	
	protected boolean blockMultiChannelListeners() { return true; }
	
	public JDA getJDA() { return this.jdaSource.get(); }
	
	public boolean isCorrectChannel(MessageChannel channel)
	{
		return this.channelID.get().equals(channel.getId());
	}
	
	public TextChannel getTextChannel()
	{
		if(this.getJDA() == null)
			return null;
		return this.getJDA().getTextChannelById(this.channelID.get());
	}
	
	public Guild getGuild()
	{
		TextChannel textChannel = this.getTextChannel();
		if(textChannel == null)
			return null;
		return textChannel.getGuild();
	}
	
	public void sendTextMessage(String message)
	{
		if(this.getJDA() == null)
			return;
		TextChannel channel = this.getTextChannel();
		if(channel != null)
			MessageUtil.sendTextMessage(channel, message);
	}
	
	public void setTopic(String topic)
	{
		if(this.getJDA() == null)
			return;
		TextChannel channel = this.getTextChannel();
		if(channel != null)
			channel.getManager().setTopic(topic).queue();
	}
	
	public void sendTextMessage(List<String> messages)
	{
		if(this.getJDA() == null)
			return;
		TextChannel channel = this.getTextChannel();
		if(channel != null)
			MessageUtil.sendTextMessage(channel, messages);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if(this.isCorrectChannel(event.getChannel()))
		{
			this.onChannelMessageReceived(event);
		}
	}
	
	protected abstract void onChannelMessageReceived(MessageReceivedEvent event);
	
}
