package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

public class CommandDiscordLink {

	public static final String COMMAND_LITERAL = "linkdiscord";
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> discordLinkCommand
			= Commands.literal("linkdiscord")
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
			source.sendSuccess(new TextComponent("You account has been successfully linked to your discord account."), true);
			return 1;
		}
		else if(output == 0)
		{
			source.sendFailure(new TextComponent(linkKey + " is not a valid link key."));
			return 0;
		}
		else if(output == -1)
		{
			source.sendFailure(new TextComponent("Your account is already linked to a discord account."));
			return 0;
		}
		return 0;
	}
	
}
