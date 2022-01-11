package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.util.text.StringTextComponent;

public class CommandDiscordUnlinkOther {

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> discordLinkCommand
			= Commands.literal("unlinkdiscordadmin")
				.requires((commandSource) -> commandSource.hasPermissionLevel(2))
						.then(Commands.argument("playerName", MessageArgument.message())
								.executes(CommandDiscordUnlinkOther::unlinkPlayerName)
				);
		
		dispatcher.register(discordLinkCommand);
	}
	
	/*static int unlinkPlayer(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		PlayerEntity player = EntityArgument.getPlayer(commandContext, "player");
		if(player == null)
		{
			commandContext.getSource().sendErrorMessage(new StringTextComponent("Not a valid player."));
		}
		LinkedAccount account = AccountManager.getLinkedAccountFromPlayer(player);
		if(account != null)
		{
			AccountManager.unlinkAccount(account);
			commandContext.getSource().sendFeedback(new StringTextComponent(player.getName().getString() + " is no longer linked to their discord account."), true);
			return 1;
		}
		else
		{
			commandContext.getSource().sendErrorMessage(new StringTextComponent("Their account is not linked to a discord account."));
		}
		return 0;
	}*/
	
	static int unlinkPlayerName(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		String playerName = MessageArgument.getMessage(commandContext, "playerName").getString();
		LinkedAccount account = AccountManager.getLinkedAccountFromMinecraftName(playerName);
		if(account != null)
		{
			AccountManager.unlinkAccount(account);
			commandContext.getSource().sendFeedback(new StringTextComponent(playerName + " is no longer linked to their discord account."), true);
			return 1;
		}
		commandContext.getSource().sendErrorMessage(new StringTextComponent("Their account is not linked to a discord account."));
		return 0;
	}
	
}
