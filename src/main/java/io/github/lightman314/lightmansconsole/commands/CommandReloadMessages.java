package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.message.MessageManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class CommandReloadMessages {

	public static final String COMMAND_LITERAL = "linkdiscord";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> discordLinkCommand
			= Commands.literal("discordreload")
				.requires((commandSource) -> commandSource.hasPermissionLevel(2))
				.executes(CommandReloadMessages::reloadMessages);
		
		dispatcher.register(discordLinkCommand);
	}
	
	static int reloadMessages(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		MessageManager.reload();
		
		commandContext.getSource().sendFeedback(new StringTextComponent("Messages have been reloaded from file."), true);
		
		return 1;
		
	}
	
}
