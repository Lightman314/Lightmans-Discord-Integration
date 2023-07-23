package io.github.lightman314.lightmansdiscord.api.jda.listeners;

import com.google.common.base.Supplier;
import io.github.lightman314.lightmansdiscord.api.jda.JDAUtil;
import io.github.lightman314.lightmansdiscord.api.jda.data.*;
import io.github.lightman314.lightmansdiscord.api.jda.data.channels.SafePrivateChannelReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.channels.SafeTextChannelReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.messages.SafeMessageReference;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public abstract class SafeSingleChannelListener extends ListenerAdapter {

    private final Supplier<String> channelID;

    protected boolean listenToPrivateMessages() { return false; }

    protected SafeSingleChannelListener(@Nonnull Supplier<String> channelID) { this.channelID = channelID; }

    @Nullable
    public final SafeTextChannelReference getChannel() { return JDAUtil.getTextChannel(this.channelID.get()); }

    public final void sendMessage(String message)
    {
        SafeTextChannelReference channel = this.getChannel();
        if(channel != null)
            channel.sendMessage(message);
    }

    public final void sendMessage(List<String> message)
    {
        SafeTextChannelReference channel = this.getChannel();
        if(channel != null)
            channel.sendMessage(message);
    }

    public final void setTopic(String topic)
    {
        SafeTextChannelReference channel = this.getChannel();
        if(channel != null)
            channel.setTopic(topic);
    }

    public final String getTopic()
    {
        SafeTextChannelReference channel = this.getChannel();
        if(channel != null)
            return channel.getTopic();
        return "";
    }

    @Nullable
    public final SafeGuildReference getGuild() {
        SafeTextChannelReference channel = this.getChannel();
        if(channel != null)
            return channel.getGuild();
        return null;
    }

    private boolean isCorrectChannel(MessageChannelUnion channel) { return Objects.equals(channel.getId(), this.channelID.get()); }

    @Override
    public final void onMessageReceived(@Nonnull MessageReceivedEvent event)
    {
        //
        if(this.listenToPrivateMessages() && event.getChannel() instanceof PrivateChannel pc)
        {
            SafePrivateChannelReference channel = SafePrivateChannelReference.of(pc);
            SafeUserReference user = SafeUserReference.of(event.getAuthor());
            SafeMessageReference message = SafeMessageReference.of(event.getMessage());
            this.OnPrivateMessage(channel, user, message);
        }
        if(this.isCorrectChannel(event.getChannel()) && event.getChannel().getType() == ChannelType.TEXT)
        {
            SafeGuildReference guild = this.getGuild();
            if(guild != null)
            {
                SafeMemberReference member = SafeMemberReference.of(event.getMember());
                if(member != null)
                    this.OnTextChannelMessage(member, SafeMessageReference.of(event.getMessage()));
            }
        }
    }

    protected abstract void OnTextChannelMessage(SafeMemberReference member, SafeMessageReference message);

    protected void OnPrivateMessage(SafePrivateChannelReference channel, SafeUserReference user, SafeMessageReference message) { }

}
