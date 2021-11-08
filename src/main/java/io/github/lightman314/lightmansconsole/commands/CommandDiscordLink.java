package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class CommandDiscordLink {

	public static final String COMMAND_LITERAL = "linkdiscord";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> discordLinkCommand
			= Commands.literal("linkdiscord")
				.requires((commandSource) -> commandSource.getEntity() instanceof PlayerEntity)
				.then(Commands.argument("linkkey", MessageArgument.message())
						.executes(CommandDiscordLink::linkPlayer)
					);
		
		dispatcher.register(discordLinkCommand);
	}
	
	static int linkPlayer(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		String linkKey = MessageArgument.getMessage(commandContext, "linkkey").getString();
		CommandSource source = commandContext.getSource();
		PlayerEntity player = source.asPlayer();
		int output = AccountManager.tryLinkUser(player, linkKey);
		if(output == 1)
		{
			source.sendFeedback(new StringTextComponent("You account has been successfully linked to your discord account."), true);
			return 1;
		}
		else if(output == 0)
		{
			source.sendErrorMessage(new StringTextComponent(linkKey + " is not a valid link key."));
			return 0;
		}
		else if(output == -1)
		{
			source.sendErrorMessage(new StringTextComponent("Your account is already linked to a discord account."));
			return 0;
		}
		return 0;
	}
	
}
