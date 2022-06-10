package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.message.MessageManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CommandReloadMessages {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> reloadCommand
			= Commands.literal("reloadmessages")
				.requires((commandSource) -> commandSource.hasPermission(2))
				.executes(CommandReloadMessages::reloadMessages);
		
		dispatcher.register(reloadCommand);
	}
	
	static int reloadMessages(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		MessageManager.reload();
		
		commandContext.getSource().sendSuccess(Component.literal("Messages have been reloaded from file."), true);
		
		return 1;
		
	}
	
}
