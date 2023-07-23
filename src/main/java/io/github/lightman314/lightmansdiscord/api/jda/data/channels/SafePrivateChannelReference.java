package io.github.lightman314.lightmansdiscord.api.jda.data.channels;

import io.github.lightman314.lightmansdiscord.api.jda.data.SafeUserReference;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;

import javax.annotation.Nullable;

public class SafePrivateChannelReference extends SafeMessageChannelReference {

    private final PrivateChannel channel;
    private SafePrivateChannelReference(PrivateChannel channel) { super(channel); this.channel = channel; }
    @Nullable
    public static SafePrivateChannelReference of(PrivateChannel channel) { if(channel != null) return new SafePrivateChannelReference(channel); return null; }

    public final SafeUserReference getUser() { return SafeUserReference.of(this.channel.getUser()); }

}
