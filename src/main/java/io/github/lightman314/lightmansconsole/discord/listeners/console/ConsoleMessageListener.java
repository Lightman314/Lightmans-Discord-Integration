package io.github.lightman314.lightmansconsole.discord.listeners.console;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansconsole.Config;
import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.discord.listeners.types.SingleChannelListener;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ConsoleMessageListener extends SingleChannelListener implements CommandSource{

	MinecraftServer server = null;
	CommandSourceStack commandSource = null;
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
		String prefix = Config.SERVER.consoleCommandPrefix.get();
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
				server.getCommands().performCommand(commandSource, command);
				this.sendTextMessage(this.output);
			}
		}
	}
	
	private CommandSourceStack getCommandSource()
	{
		ServerLevel world = server.overworld();
		return new CommandSourceStack(this, world == null ? Vec3.ZERO : Vec3.atBottomCenterOf(world.getSharedSpawnPos()), Vec2.ZERO, world, 4, "ConsoleBot", new TextComponent("ConsoleBot"), server, null);
	}

	@Override
	public void sendMessage(Component component, UUID senderUUID) {
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
