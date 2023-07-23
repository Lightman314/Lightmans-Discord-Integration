package io.github.lightman314.lightmansdiscord.events;

import io.github.lightman314.lightmansdiscord.proxy.Proxy;
import net.dv8tion.jda.api.JDA;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class JDAInitializedEvent extends Event{

	private final Proxy proxy;

	/**
	 * @deprecated Use JDAInitializedEvent.addListener instead of going through the proxy.
	 */
	@Deprecated(since = "0.2.0.0")
	public Proxy getProxy() { return this.proxy; }
	/**
	 * @deprecated Try to avoid accessing the JDA when possible to
	 * avoid crashes to addon mods when Discord makes internal changes.
	 */
	@Deprecated(since = "0.2.0.0")
	public JDA getJDA() { return this.proxy.getJDA(); }
	
	public JDAInitializedEvent(Proxy proxy) { this.proxy = proxy; }

	/**
	 * Registers the listener to the JDA only
	 * Use addListener(listener, true) to register to both JDA and the Forge EVENT_BUS.
	 */
	public final void addListener(Object listener) { this.addListener(listener, false); }

	/**
	 * Registers the listener to the JDA
	 * If listenToForge is true, it will also register the listener to the Forge EVENT_BUS
	 */
	public final void addListener(Object listener, boolean listenToForge)
	{
		this.proxy.addListener(listener);
		if(listenToForge)
			MinecraftForge.EVENT_BUS.register(listener);
	}


	
}
