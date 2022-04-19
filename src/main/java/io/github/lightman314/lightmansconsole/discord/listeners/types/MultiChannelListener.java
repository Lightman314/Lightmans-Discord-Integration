package io.github.lightman314.lightmansconsole.discord.listeners.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MultiChannelListener extends ListenerAdapter{

	private static final List<Supplier<String>> ignoreChannels = new ArrayList<>();
	public static void ignoreChannel(Supplier<String> channel) { if(!ignoreChannels.contains(channel)) ignoreChannels.add(channel); }
	public static void allowChannel(Supplier<String> channel) { ignoreChannels.remove(channel); }
	
	public static List<String> convertListType(List<? extends String> list) {
		List<String> result = new ArrayList<>();
		for(String value : list) {
			result.add(value);
		}
		return result;
	}
	
	public static List<String> getIgnoredChannels() { 
		List<String> list = new ArrayList<>();
		for(Supplier<String> ignoreSource : ignoreChannels) {
			String value = ignoreSource.get();
			if(value != null)
				list.add(value);
		}
		return list;
	}
	
	public static boolean canListenToChannel(List<? extends String> blacklistConfig, String channelID) {
		List<String> blacklist = convertListType(blacklistConfig);
		blacklist.addAll(getIgnoredChannels());
		return canListenToChannelInternal(blacklist, channelID);
	}
	
	public static boolean canListenToChannel(String channelID) {
		return canListenToChannelInternal(getIgnoredChannels(), channelID);
	}
	
	private static boolean canListenToChannelInternal(List<String> blacklist, String channelID) {
		for(String channel : blacklist)
		{
			if(channel.equals(channelID))
				return false;
		}
		return true;
	}
	
}
