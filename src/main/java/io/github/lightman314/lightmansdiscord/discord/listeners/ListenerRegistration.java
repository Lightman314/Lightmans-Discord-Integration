package io.github.lightman314.lightmansdiscord.discord.listeners;

import io.github.lightman314.lightmansdiscord.LDIConfig;
import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import io.github.lightman314.lightmansdiscord.discord.listeners.account.AccountMessageListener;
import io.github.lightman314.lightmansdiscord.discord.listeners.chat.ChatMessageListener;
import io.github.lightman314.lightmansdiscord.discord.listeners.console.ConsoleMessageListener;
import io.github.lightman314.lightmansdiscord.events.JDAInitializedEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ListenerRegistration {

	@SubscribeEvent
	@OnlyIn(Dist.DEDICATED_SERVER)
	public static void onJDAInit(JDAInitializedEvent event)
	{
		try {
			//Add the console listener
			event.addListener(new ConsoleMessageListener(LDIConfig.SERVER.consoleChannel::get), false);
			//Add the chat listener
			event.addListener(new ChatMessageListener(LDIConfig.SERVER.chatChannel::get), true);
			//Add the account linking listeners
	    	event.addListener(new AccountMessageListener(), false);
	    	//event.getProxy().addListener(new AccountAdminListener(Config.SERVER.accountAdminChannel.get()));
		} catch(Throwable t) { LightmansDiscordIntegration.LOGGER.error("Error loading included JDA listeners.", t); }
	}


	
}
