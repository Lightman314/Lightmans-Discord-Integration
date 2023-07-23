package io.github.lightman314.lightmansdiscord;

import com.mojang.brigadier.CommandDispatcher;

import io.github.lightman314.lightmansdiscord.commands.CommandDiscordLink;
import io.github.lightman314.lightmansdiscord.commands.CommandDiscordList;
import io.github.lightman314.lightmansdiscord.commands.CommandDiscordMessage;
import io.github.lightman314.lightmansdiscord.commands.CommandDiscordUnlinkOther;
import io.github.lightman314.lightmansdiscord.commands.CommandDiscordUnlinkSelf;
import io.github.lightman314.lightmansdiscord.commands.CommandReloadMessages;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ForgeEventListener {

	 @SubscribeEvent(priority = EventPriority.LOWEST)
	 public static void onServerStop(ServerStoppedEvent event)
	 {
		 if(LightmansDiscordIntegration.PROXY.getJDA() != null)
		 {
			 LightmansDiscordIntegration.PROXY.getJDA().shutdownNow();
			 LightmansDiscordIntegration.PROXY.clearJDA();
		 }
	 }
	 
	 @SubscribeEvent
	 public static void onCommandRegister(RegisterCommandsEvent event)
	 {
		 CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
		 //Link command
		 CommandDiscordLink.register(commandDispatcher);
		 //Unlink self command
		 CommandDiscordUnlinkSelf.register(commandDispatcher);
		 //Unlink other command
		 CommandDiscordUnlinkOther.register(commandDispatcher);
		 //List command
		 CommandDiscordList.register(commandDispatcher);
		 //Reload Messages Command
		 CommandReloadMessages.register(commandDispatcher);
		 //Discord Messages Command
		 CommandDiscordMessage.register(commandDispatcher);
	 }
	
}
