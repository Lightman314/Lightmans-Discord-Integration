package io.github.lightman314.lightmansdiscord.api.jda.data.channels;

import io.github.lightman314.lightmansdiscord.api.jda.data.messages.SafeMessageHistoryReference;
import io.github.lightman314.lightmansdiscord.util.MessageUtil;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import javax.annotation.Nullable;
import java.util.List;

public class SafeMessageChannelReference {

    private final MessageChannel channel;
    protected SafeMessageChannelReference(MessageChannel channel) { this.channel = channel; }
    @Nullable
    public static SafeMessageChannelReference of(@Nullable MessageChannel channel) { if(channel != null) return new SafeMessageChannelReference(channel); return null; }

    public final String getID() { return this.channel.getId(); }

    public final SafeMessageHistoryReference getHistory() { return SafeMessageHistoryReference.of(this.channel.getHistory()); }

    public final void sendMessage(List<String> message) { MessageUtil.sendTextMessage(this.channel, message); }

    public final void sendMessage(String message) { MessageUtil.sendTextMessage(this.channel, message); }

    public final String getName() { return this.channel.getName(); }

}
