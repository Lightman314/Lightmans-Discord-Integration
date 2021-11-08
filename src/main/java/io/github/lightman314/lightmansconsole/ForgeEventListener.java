package io.github.lightman314.lightmansconsole;

import com.mojang.brigadier.CommandDispatcher;

import io.github.lightman314.lightmansconsole.commands.CommandDiscordLink;
import io.github.lightman314.lightmansconsole.commands.CommandDiscordList;
import io.github.lightman314.lightmansconsole.commands.CommandDiscordUnlinkOther;
import io.github.lightman314.lightmansconsole.commands.CommandDiscordUnlinkSelf;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

@Mod.EventBusSubscriber
public class ForgeEventListener {

	 @SubscribeEvent
	 public static void onServerStop(FMLServerStoppedEvent event)
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
		 CommandDispatcher<CommandSource> commandDispatcher = event.getDispatcher();
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
