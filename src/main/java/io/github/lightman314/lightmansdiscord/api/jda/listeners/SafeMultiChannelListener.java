package io.github.lightman314.lightmansdiscord.api.jda.listeners;

import com.google.common.base.Supplier;
import io.github.lightman314.lightmansdiscord.api.jda.data.*;
import io.github.lightman314.lightmansdiscord.api.jda.data.channels.SafeMessageChannelReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.channels.SafeTextChannelReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.messages.SafeMessageReference;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class SafeMultiChannelListener extends ListenerAdapter {

    private static final List<Supplier<String>> ignoreChannels = new ArrayList<>();
    public static void ignoreChannel(Supplier<String> channel) { if(!ignoreChannels.contains(channel)) ignoreChannels.add(channel); }
    public static void allowChannel(Supplier<String> channel) { ignoreChannels.remove(channel); }



    public static List<String> convertListType(List<? extends String> list) { return new ArrayList<>(list); }

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

    protected enum MessageType { UNKNOWN, GUILD, PRIVATE }

    @Override
    public final void onMessageReceived(@Nonnull MessageReceivedEvent event)
    {
        if(canListenToChannel(event.getChannel().getId()))
        {
            SafeMessageReference message = SafeMessageReference.of(event.getMessage());
            if(event.isFromGuild())
            {
                SafeTextChannelReference channel = SafeTextChannelReference.of(event.getChannel());
                SafeMemberReference user = SafeMemberReference.of(event.getMember());
                this.OnMessage(channel, user, message, MessageType.GUILD);
            }
            else
            {
                SafeMessageChannelReference channel = SafeMessageChannelReference.of(event.getChannel());
                SafeUserReference user = SafeUserReference.of(event.getAuthor());
                this.OnMessage(channel, user, message, event.getChannel().getType() == ChannelType.PRIVATE ? MessageType.PRIVATE : MessageType.UNKNOWN);
            }
        }
    }

    protected abstract void OnMessage(SafeMessageChannelReference channel, SafeUserReference user, SafeMessageReference message, MessageType type);



}
