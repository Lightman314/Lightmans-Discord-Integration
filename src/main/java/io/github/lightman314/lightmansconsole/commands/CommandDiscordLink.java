package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.PlayerEntity;

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
			String discordName = "ERROR";
			LinkedAccount account = AccountManager.getLinkedAccountFromPlayer(player);
			if(account != null)
				discordName = account.getMemberName();
			source.sendFeedback(MessageManager.M_COMMAND_LINK_COMPLETE.formatComponent(discordName), true);
			return 1;
		}
		else if(output == 0)
		{
			source.sendErrorMessage(MessageManager.M_COMMAND_LINK_BADKEY.formatComponent(linkKey));
			return 0;
		}
		else if(output == -1)
		{
			String discordName = "ERROR";
			LinkedAccount account = AccountManager.getLinkedAccountFromPlayer(player);
			if(account != null)
				discordName = account.getMemberName();
			source.sendErrorMessage(MessageManager.M_COMMAND_LINK_ALREADYLINKED.formatComponent(discordName));
			return 0;
		}
		return 0;
	}
	
}
