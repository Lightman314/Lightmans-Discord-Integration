package io.github.lightman314.lightmansconsole.discord.listeners.console;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansconsole.LDIConfig;
import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.discord.listeners.types.SingleChannelListener;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ConsoleMessageListener extends SingleChannelListener implements ICommandSource{

	MinecraftServer server = null;
	CommandSource commandSource = null;
	List<String> output = new ArrayList<>();
	
	
	public ConsoleMessageListener(Supplier<String> consoleChannel)
	{
		super(consoleChannel, () -> LightmansDiscordIntegration.PROXY.getJDA());
		server = ServerLifecycleHooks.getCurrentServer();
		commandSource = this.getCommandSource();
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
			command = command.substring(prefix.length(), command.length());
			if(command.startsWith("mchelp")) //Manual replacement of mchelp with help
				command = command.substring(2, command.length());
			//LightmansConsole.LOGGER.info("Received Command: '" + command + "'");
			if(server == null)
				LightmansDiscordIntegration.LOGGER.error("Server is null!");
			else
			{
				this.output.clear();
				server.getCommandManager().handleCommand(commandSource, command);
				this.sendTextMessage(this.output);
			}
		}
	}
	
	private CommandSource getCommandSource()
	{
		ServerWorld world = server.func_241755_D_();
		return new CommandSource(this, world == null ? Vector3d.ZERO : Vector3d.copy(world.getSpawnPoint()), Vector2f.ZERO, world, 4, "ConsoleBot", new StringTextComponent("ConsoleBot"), server, (Entity)null);
	}

	@Override
	public void sendMessage(ITextComponent component, UUID senderUUID) {
		this.output.add(component.getString());
		//LightmansConsole.LOGGER.info("Added '" + component.getString() + "' to the output list.");
	}

	@Override
	public boolean shouldReceiveFeedback() {
		return true;
	}

	@Override
	public boolean shouldReceiveErrors() {
		return true;
	}

	@Override
	public boolean allowLogging() {
		return true;
	}
	
}
