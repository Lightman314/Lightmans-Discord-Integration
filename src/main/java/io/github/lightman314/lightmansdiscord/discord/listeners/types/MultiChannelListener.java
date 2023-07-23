package io.github.lightman314.lightmansdiscord.discord.listeners.types;

import java.util.List;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansdiscord.api.jda.listeners.SafeMultiChannelListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Deprecated(since = "0.1.1.0")
public class MultiChannelListener extends ListenerAdapter{

	public static void ignoreChannel(Supplier<String> channel) { SafeMultiChannelListener.ignoreChannel(channel); }
	public static void allowChannel(Supplier<String> channel) { SafeMultiChannelListener.allowChannel(channel); }

	public static List<String> getIgnoredChannels() { return SafeMultiChannelListener.getIgnoredChannels(); }
	
	public static boolean canListenToChannel(List<? extends String> blacklistConfig, String channelID) { return SafeMultiChannelListener.canListenToChannel(blacklistConfig, channelID); }
	
	public static boolean canListenToChannel(String channelID) { return SafeMultiChannelListener.canListenToChannel(channelID); }
	
	private static boolean canListenToChannelInternal(List<String> blacklist, String channelID) {
		for(String channel : blacklist)
		{
			if(channel.equals(channelID))
				return false;
		}
		return true;
	}

	protected boolean allowPrivateMessages() { return true; }
	
}
