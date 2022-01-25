package io.github.lightman314.lightmansconsole.events;

import net.minecraftforge.eventbus.api.Event;

public class CreateMessageEntriesEvent extends Event{
	
	/**
	 * Called just before the initial load of the message data.
	 * Listen to this event to ensure any custom messages you wish to add get created before the file is loaded, as any missing entries will be deleted from the file.
	 */
	public CreateMessageEntriesEvent() { }
	
}
