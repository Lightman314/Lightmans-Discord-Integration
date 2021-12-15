package io.github.lightman314.lightmansconsole;

import com.mojang.brigadier.CommandDispatcher;

import io.github.lightman314.lightmansconsole.commands.CommandDiscordLink;
import io.github.lightman314.lightmansconsole.commands.CommandDiscordList;
import io.github.lightman314.lightmansconsole.commands.CommandDiscordUnlinkOther;
import io.github.lightman314.lightmansconsole.commands.CommandDiscordUnlinkSelf;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ForgeEventListener {

	 @SubscribeEvent
	 public static void onServerStop(ServerStoppedEvent event)
	 {
		 if(LightmansConsole.PROXY.getJDA() != null)
		 {
			 LightmansConsole.PROXY.getJDA().shutdownNow();
			 LightmansConsole.PROXY.clearJDA();
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
		 
	 }
	
}
