package io.github.lightman314.lightmansconsole.discord.listeners.account;

import io.github.lightman314.lightmansconsole.discord.listeners.account.AccountMessageListener.RegisterAccountCommandEvent;
import io.github.lightman314.lightmansconsole.discord.listeners.account.commands.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RegisterAccountCommands {

	@SubscribeEvent
	public static void onCommandRegistry(RegisterAccountCommandEvent event)
	{
		event.registerCommand(new IGNCommand());
		event.registerCommand(new DiscordNameCommand());
		//event.registerCommand(new StatsCommand());
	}
	
}
