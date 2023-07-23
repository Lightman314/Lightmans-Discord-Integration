package io.github.lightman314.lightmansdiscord.api.jda.data.messages;

import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nullable;

public class SafeMessageReference {

    private final Message message;
    public final Message getMessage() { return this.message; }
    private SafeMessageReference(Message message) { this.message = message; }
    @Nullable
    public static SafeMessageReference of(Message message) { if(message != null) return new SafeMessageReference(message); return null; }

    public final String getID() { return this.message.getId(); }

    public final String getRaw() { return this.message.getContentRaw(); }
    public final String getDisplay() { return this.message.getContentDisplay(); }
    public final String getStripped() { return this.message.getContentStripped(); }
}
