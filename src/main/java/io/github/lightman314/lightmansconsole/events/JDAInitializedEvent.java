package io.github.lightman314.lightmansconsole.events;

import io.github.lightman314.lightmansconsole.proxy.Proxy;
import net.dv8tion.jda.api.JDA;
import net.minecraftforge.eventbus.api.Event;

public class JDAInitializedEvent extends Event{

	final Proxy proxy;
	public Proxy getProxy() { return this.proxy; }
	public JDA getJDA() { return this.proxy.getJDA(); }
	
	public JDAInitializedEvent(Proxy proxy)
	{
		this.proxy = proxy;
	}
	
}
