package io.github.lightman314.lightmansdiscord.message;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class MessageEntry
{
    public final String key;
    public final String comment;
    public final String defaultValue;

    private String currentValue;
    public void loadCurrentValue(@Nonnull String value) { this.currentValue = value; }
    public String getCurrentValue() { return this.currentValue; }

    private final List<String> formatKeys;

    public String get() { return format(); }

    public String format(Object... format)
    {
        String result = this.currentValue;
        //Replace \n with a new line
        result = result.replace("\\n", "\n");
        for(int i = 0; i < formatKeys.size() && i < format.length; ++i)
        {
            String formatText = format[i].toString();
            if(format[i] instanceof Component) //Check if it's a text component, and if so, run .getString() instead of .toString();
                formatText = ((Component)format[i]).getString();
            result = result.replace("{" + formatKeys.get(i) + "}", formatText);
        }

        return result;
    }

    public MutableComponent getComponent() { return new TextComponent(get()); }

    public MutableComponent formatComponent(Object... format) { return new TextComponent(format(format)); }

    protected MessageEntry(@Nonnull String key, @Nonnull String comment, @Nonnull String defaultValue, @Nonnull String... formatKeys)
    {
        this.key = key; this.comment = comment; this.defaultValue = this.currentValue = defaultValue;
        this.formatKeys = Lists.newArrayList(formatKeys);
    }

    public static MessageEntry create(@Nonnull String key, @Nonnull String comment, @Nonnull String defaultValue, @Nonnull String... formatKeys) { return new MessageEntry(key, comment, defaultValue, formatKeys); }
    public static MessageEntry create(@Nullable List<MessageEntry> list, @Nonnull String key, @Nonnull String comment, @Nonnull String defaultValue, @Nonnull String... formatKeys) { return create(list == null ? null : list::add, key, comment, defaultValue, formatKeys); }
    public static MessageEntry create(@Nullable Consumer<MessageEntry> consumer, @Nonnull String key, @Nonnull String comment, @Nonnull String defaultValue, @Nonnull String... formatKeys)
    {
        MessageEntry entry = create(key, comment, defaultValue, formatKeys);
        if(consumer != null)
            consumer.accept(entry);
        return entry;
    }

    @Nullable
    public static MessageEntry getEntry(@Nullable List<MessageEntry> list, @Nonnull String key)
    {
        if(list == null)
            return null;
        for(MessageEntry entry : list)
        {
            if(entry.key.contentEquals(key))
                return entry;
        }
        return null;
    }

}
