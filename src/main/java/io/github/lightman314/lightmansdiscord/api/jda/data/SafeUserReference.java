package io.github.lightman314.lightmansdiscord.api.jda.data;

import io.github.lightman314.lightmansdiscord.util.MessageUtil;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;
import java.util.List;

public class SafeUserReference {

    private final User user;
    public final User getUser() { return this.user; }
    protected SafeUserReference(User user) { this.user = user; }
    @Nullable
    public static SafeUserReference of(User user) { if(user != null) return new SafeUserReference(user); return null; }

    public final String getID() { return this.user.getId(); }

    public final boolean isBot() { return this.user.isBot(); }

    public final String getName() { return this.user.getName(); }
    public final String getDiscriminator() { return this.user.getDiscriminator(); }

    public void sendPrivateMessage(String message) { MessageUtil.sendPrivateMessage(this.user, message); }
    public void sendPrivateMessage(List<String> message) { MessageUtil.sendPrivateMessage(this.user, message); }

}
