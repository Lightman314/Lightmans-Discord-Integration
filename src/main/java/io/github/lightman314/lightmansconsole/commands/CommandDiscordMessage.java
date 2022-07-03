package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.listeners.chat.ChatMessageListener;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;

public class CommandDiscordMessage {

	public static final String COMMAND_LITERAL = "telldiscord";
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> discordMessageCommand
			= Commands.literal(COMMAND_LITERAL)
				.then(Commands.argument("message", MessageArgument.message())
						.executes(CommandDiscordMessage::sendMessage)
						);
		
		dispatcher.register(discordMessageCommand);
		
	}
	
	static int sendMessage(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		String message = MessageArgument.getMessage(commandContext, "message").getString();
		
		ChatMessageListener.sendChatMessage(message);
		
		return 1;
		
	}
	
}
