package io.github.lightman314.lightmansdiscord.api.jda.data;

import io.github.lightman314.lightmansdiscord.api.jda.data.channels.SafeTextChannelReference;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SafeGuildReference {

    private final Guild guild;
    public final Guild getGuild() { return this.guild; }
    private SafeGuildReference(Guild guild) { this.guild = guild; }
    @Nullable
    public static SafeGuildReference of(Guild guild) { if(guild != null) return new SafeGuildReference(guild); return null; }

    public final String getID() { return this.guild.getId(); }

    public final SafeMemberReference getMember(String id) { if(id == null) return null; return SafeMemberReference.of(this.guild.getMemberById(id)); }

    public final SafeTextChannelReference getChannel(String id) { if(id == null) return null; return SafeTextChannelReference.of(this.guild.getTextChannelById(id)); }

    public final List<SafeTextChannelReference> getChannels()
    {
        List<GuildChannel> channels = this.guild.getChannels();
        List<SafeTextChannelReference> result = new ArrayList<>();
        for(GuildChannel c : channels)
        {
            if(c instanceof TextChannel tc)
                result.add(SafeTextChannelReference.of(tc));
        }
        return result;
    }

    public final List<SafeRoleReference> getRoles() { return this.guild.getRoles().stream().map(SafeRoleReference::of).toList(); }
    public final SafeRoleReference getRole(String id) { if(id == null) return null; return SafeRoleReference.of(this.guild.getRoleById(id)); }

    public final List<SafeMemberReference> getMembers() { return this.guild.getMembers().stream().map(SafeMemberReference::of).toList(); }

    public final List<SafeMemberReference> getMembersByName(String name) { return this.guild.getMembersByName(name, true).stream().map(SafeMemberReference::of).toList(); }

    public final List<SafeMemberReference> getMembersByEffectiveName(String name) { return this.guild.getMembersByEffectiveName(name, true).stream().map(SafeMemberReference::of).toList(); }

}
