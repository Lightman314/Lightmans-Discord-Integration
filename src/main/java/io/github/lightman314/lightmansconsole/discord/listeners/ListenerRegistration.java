package io.github.lightman314.lightmansconsole.discord.listeners;

import io.github.lightman314.lightmansconsole.Config;
import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.discord.listeners.account.AccountMessageListener;
import io.github.lightman314.lightmansconsole.discord.listeners.chat.ChatMessageListener;
import io.github.lightman314.lightmansconsole.discord.listeners.console.ConsoleMessageListener;
import io.github.lightman314.lightmansconsole.discord.listeners.currency.CurrencyListener;
import io.github.lightman314.lightmansconsole.events.JDAInitializedEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ListenerRegistration {

	@SubscribeEvent
	@OnlyIn(Dist.DEDICATED_SERVER)
	public static void onJDAInit(JDAInitializedEvent event)
	{
		//Add the console listener
    	event.getProxy().addListener(new ConsoleMessageListener(Config.SERVER.consoleChannel::get));
		//Add the chat listener
    	ChatMessageListener cml = new ChatMessageListener(Config.SERVER.chatChannel::get);
    	MinecraftForge.EVENT_BUS.register(cml);
    	event.getProxy().addListener(cml);
		//Add the account linking listeners
    	event.getProxy().addListener(new AccountMessageListener());
    	//event.getProxy().addListener(new AccountAdminListener(Config.SERVER.accountAdminChannel.get()));
    	if(LightmansDiscordIntegration.isLightmansCurrencyLoaded())
    	{
    		CurrencyListener cl = new CurrencyListener(Config.SERVER.currencyChannel::get);
    		MinecraftForge.EVENT_BUS.register(cl);
    		event.getProxy().addListener(cl);
    	}
	}
	
}
