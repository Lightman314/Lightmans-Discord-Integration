package io.github.lightman314.lightmansdiscord.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansdiscord.discord.listeners.chat.ChatMessageListener;
import io.github.lightman314.lightmansdiscord.message.MessageManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;

public class CommandDiscordMessage {

	public static final String COMMAND_LITERAL = "telldiscord";
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> discordMessageCommand
			= Commands.literal(COMMAND_LITERAL)
				.requires(c -> c.hasPermission(2) && ChatMessageListener.isPresent())
				.then(Commands.argument("message", MessageArgument.message())
						.executes(CommandDiscordMessage::sendMessage)
						);
		
		dispatcher.register(discordMessageCommand);
		
	}
	
	static int sendMessage(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		String message = MessageArgument.getMessage(commandContext, "message").getString();
		
		ChatMessageListener.sendChatMessage(message);
		
		commandContext.getSource().sendSuccess(MessageManager.M_COMMAND_TELLDISCORD.formatComponent(message), true);
		
		return 1;
		
	}
	
}
