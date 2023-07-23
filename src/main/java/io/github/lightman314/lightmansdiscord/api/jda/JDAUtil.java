package io.github.lightman314.lightmansdiscord.api.jda;

import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import io.github.lightman314.lightmansdiscord.api.jda.data.*;
import io.github.lightman314.lightmansdiscord.api.jda.data.channels.SafeTextChannelReference;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

import javax.annotation.Nullable;

public class JDAUtil {

    private JDAUtil() {}

    @Nullable
    public static JDA getJDA() { return LightmansDiscordIntegration.PROXY.getJDA(); }

    @Nullable
    public static SafeGuildReference getGuild(String guildID) {
        if(guildID == null)
            return null;
        JDA jda = getJDA();
        if(jda != null)
            return SafeGuildReference.of(jda.getGuildById(guildID));
        return null;
    }

    @Nullable
    public static SafeUserReference getUser(String userID) {
        if(userID == null)
            return null;
        JDA jda = getJDA();
        if(jda != null)
            return SafeUserReference.of(jda.getUserById(userID));
        return null;
    }

    @Nullable
    public static SafeMemberReference getMember(@Nullable SafeGuildReference guild, String userID)
    {
        if(guild != null)
            return guild.getMember(userID);
        return null;
    }

    @Nullable
    public static SafeTextChannelReference getTextChannel(String channelID)
    {
        if(channelID == null)
            return null;
        JDA jda = getJDA();
        if(jda != null)
            return SafeTextChannelReference.of(jda.getTextChannelById(channelID));
        return null;
    }

    public static void SetActivity(ActivityType type, String text, String extra)
    {
        JDA jda = JDAUtil.getJDA();
        if(jda != null)
        {
            switch (type) {
                case LISTENING -> jda.getPresence().setActivity(Activity.listening(text));
                case PLAYING -> jda.getPresence().setActivity(Activity.playing(text));
                case WATCHING -> jda.getPresence().setActivity(Activity.watching(text));
                case COMPETING -> jda.getPresence().setActivity(Activity.competing(text));
                case STREAMING -> jda.getPresence().setActivity(Activity.streaming(text, extra));
                default -> {} //Do nothing if disabled
            }
        }
    }

    public enum ActivityType { DISABLED, LISTENING, PLAYING, WATCHING, COMPETING, STREAMING }

}
