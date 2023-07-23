package io.github.lightman314.lightmansdiscord.events;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import io.github.lightman314.lightmansdiscord.message.MessageEntry;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class LoadMessageEntriesEvent extends Event implements IModBusEvent {

	private final List<MessageEntry> entries = new ArrayList<>();
	public ImmutableList<MessageEntry> getEntries() { return ImmutableList.copyOf(this.entries); }
	public MessageEntry getEntry(@Nonnull String key) { return MessageEntry.getEntry(this.entries, key); }

	/**
	 * Called before every reload of the message data file.
	 * Listen to this to ensure all of your message entries get registered properly before the file is loaded.
	 */
	public LoadMessageEntriesEvent() { }

	public void register(@Nonnull List<MessageEntry> entries)
	{
		for(MessageEntry entry : entries)
		{
			if(entry != null)
				this.register(entry);
		}
	}

	public void register(@Nonnull MessageEntry entry)
	{
		if(!this.entries.contains(entry))
		{
			if(this.getEntry(entry.key) != null)
			{
				LightmansDiscordIntegration.LOGGER.error("Tried to register message entry with a duplicate key '" + entry.key + "'!");
				return;
			}
			this.entries.add(entry);
			LightmansDiscordIntegration.LOGGER.debug("Registered message entry '" + entry.key + "'!");
		}
		else
			LightmansDiscordIntegration.LOGGER.warn("Tried to register the message entry with key '" + entry.key + "' twice!");
	}
	
}