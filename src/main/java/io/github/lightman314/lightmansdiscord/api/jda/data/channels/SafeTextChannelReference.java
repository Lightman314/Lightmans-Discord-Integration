package io.github.lightman314.lightmansdiscord.api.jda.data.channels;

import io.github.lightman314.lightmansdiscord.api.jda.data.SafeGuildReference;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SafeTextChannelReference extends SafeMessageChannelReference {

    private final TextChannel channel;
    private SafeTextChannelReference(TextChannel channel) { super(channel); this.channel = channel; }
    @Nullable
    public static SafeTextChannelReference of(@Nullable TextChannel channel) { if(channel != null) return new SafeTextChannelReference(channel); return null; }
    @Nullable
    public static SafeTextChannelReference of(@Nullable MessageChannelUnion channel) { if(channel instanceof TextChannel tc) return of(tc); return null; }

    public final void setTopic(String topic) { this.channel.getManager().setTopic(topic).queue(); }

    public final String getTopic() { return this.channel.getTopic(); }

    @Nonnull
    public final SafeGuildReference getGuild() { return SafeGuildReference.of(this.channel.getGuild()); }


}
