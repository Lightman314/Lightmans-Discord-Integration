package io.github.lightman314.lightmansdiscord.discord.listeners.console;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansdiscord.LDIConfig;
import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeMemberReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.messages.SafeMessageReference;
import io.github.lightman314.lightmansdiscord.api.jda.listeners.SafeSingleChannelListener;
import io.github.lightman314.lightmansdiscord.message.MessageManager;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

public class ConsoleMessageListener extends SafeSingleChannelListener implements CommandSource{

	MinecraftServer server = null;
	protected final void checkForServer() { if(this.server == null) this.server = ServerLifecycleHooks.getCurrentServer(); }
	List<String> output = new ArrayList<>();

	public static ConsoleMode getMode() { return ConsoleMode.COMMANDS_ONLY; }

	public ConsoleMessageListener(Supplier<String> consoleChannel)
	{
		super(consoleChannel);
		if(getMode().acceptCommands)
			this.sendMessage(MessageManager.M_CONSOLEBOT_READY.get());
		this.createLogAppender();
	}

	private void createLogAppender()
	{
		//TODO attempt to add log appender?
	}

	@Override
	protected void OnTextChannelMessage(SafeMemberReference member, SafeMessageReference message) {
		LightmansDiscordIntegration.LOGGER.debug("Received message '" + message.getRaw() + "' in console channel!\nCommand Prefix is '" + LDIConfig.SERVER.consoleCommandPrefix.get() + "', and " + (message.getRaw().startsWith(LDIConfig.SERVER.consoleCommandPrefix.get()) ? "does" : "does not") + " match!");
		if(member == null || member.isBot())
			return;
		if(!getMode().acceptCommands)
			return;
		//Run command
		String command = message.getRaw();
		String prefix = LDIConfig.SERVER.consoleCommandPrefix.get();
		if(command.startsWith(prefix))
		{
			command = command.substring(prefix.length());
			if(command.startsWith("mchelp")) //Manual replacement of mchelp with help
				command = command.substring(2);
			LightmansDiscordIntegration.LOGGER.info("Received Command: '" + command + "' from Discord!");
			this.checkForServer();
			if(this.server == null)
				LightmansDiscordIntegration.LOGGER.error("Server is null!");
			else
			{
				this.output.clear();
				this.server.getCommands().performCommand(this.getCommandSource(), command);
				this.sendMessage(this.output);
			}
		}
	}

	private CommandSourceStack getCommandSource()
	{
		ServerLevel world = this.server.overworld();
		return new CommandSourceStack(this, Vec3.atBottomCenterOf(world.getSharedSpawnPos()), Vec2.ZERO, world, 4, "ConsoleBot", new TextComponent("ConsoleBot"), server, null);
	}

	@Override
	public void sendMessage(Component component, @Nonnull UUID id) { this.output.add(component.getString()); }

	@Override
	public boolean acceptsSuccess() { return true; }

	@Override
	public boolean acceptsFailure() { return true; }

	@Override
	public boolean shouldInformAdmins() { return true; }
	
}
