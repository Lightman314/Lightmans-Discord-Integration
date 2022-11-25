package io.github.lightman314.lightmansconsole.discord.listeners.console;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansconsole.LDIConfig;
import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.discord.listeners.types.SingleChannelListener;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ConsoleMessageListener extends SingleChannelListener implements CommandSource{

	MinecraftServer server;
	List<String> output = new ArrayList<>();
	
	
	public ConsoleMessageListener(Supplier<String> consoleChannel)
	{
		super(consoleChannel, LightmansDiscordIntegration.PROXY::getJDA);
		this.server = ServerLifecycleHooks.getCurrentServer();
		this.sendTextMessage(MessageManager.M_CONSOLEBOT_READY.get());
	}
	
	@Override
	public void onChannelMessageReceived(MessageReceivedEvent event)
	{
		//LightmansConsole.LOGGER.info("ConsoleMessageListener.onMessageReceived");
		if(event.getAuthor().isBot())
			return;
		//Run command
		String command = event.getMessage().getContentDisplay();
		String prefix = LDIConfig.SERVER.consoleCommandPrefix.get();
		if(command.startsWith(prefix))
		{
			command = command.substring(prefix.length());
			if(command.startsWith("mchelp")) //Manual replacement of mchelp with help
				command = command.substring(2);
			//LightmansConsole.LOGGER.info("Received Command: '" + command + "'");
			if(server == null)
				LightmansDiscordIntegration.LOGGER.error("Server is null!");
			else
			{
				this.output.clear();
				this.server.getCommands().performPrefixedCommand(this.getCommandSource(), command);
				this.sendTextMessage(this.output);
			}
		}
	}

	private CommandSourceStack getCommandSource()
	{
		ServerLevel world = this.server.overworld();
		return new CommandSourceStack(this, Vec3.atBottomCenterOf(world.getSharedSpawnPos()), Vec2.ZERO, world, 4, "ConsoleBot", Component.literal("ConsoleBot"), server, null);
	}

	@Override
	public void sendSystemMessage(Component component) {
		this.output.add(component.getString());
	}

	@Override
	public boolean acceptsSuccess() {
		return true;
	}

	@Override
	public boolean acceptsFailure() {
		return true;
	}

	@Override
	public boolean shouldInformAdmins() {
		return true;
	}
	
}
