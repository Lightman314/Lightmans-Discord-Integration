package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.world.entity.player.Player;

public class CommandDiscordLink {

	public static final String COMMAND_LITERAL = "linkdiscord";
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> discordLinkCommand
			= Commands.literal(COMMAND_LITERAL)
				.requires((commandSource) -> commandSource.getEntity() instanceof Player)
				.then(Commands.argument("linkkey", MessageArgument.message())
						.executes(CommandDiscordLink::linkPlayer)
					);
		
		dispatcher.register(discordLinkCommand);
	}
	
	static int linkPlayer(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		String linkKey = MessageArgument.getMessage(commandContext, "linkkey").getString();
		CommandSourceStack source = commandContext.getSource();
		Player player = source.getPlayerOrException();
		int output = AccountManager.tryLinkUser(player, linkKey);
		if(output == 1)
		{
			String discordName = "ERROR";
			LinkedAccount account = AccountManager.getLinkedAccountFromPlayer(player);
			if(account != null)
				discordName = account.getMemberName();
			source.sendSuccess(MessageManager.M_COMMAND_LINK_COMPLETE.formatComponent(discordName), true);
			return 1;
		}
		else if(output == 0)
		{
			source.sendFailure(MessageManager.M_COMMAND_LINK_BADKEY.formatComponent(linkKey));
			return 0;
		}
		else if(output == -1)
		{
			String discordName = "ERROR";
			LinkedAccount account = AccountManager.getLinkedAccountFromPlayer(player);
			if(account != null)
				discordName = account.getMemberName();
			source.sendFailure(MessageManager.M_COMMAND_LINK_ALREADYLINKED.formatComponent(discordName));
			return 0;
		}
		return 0;
	}
	
}
