package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.links.PartialLinkedAccount;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.TextComponent;

public class CommandDiscordUnlinkOther {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> discordLinkCommand
			= Commands.literal("unlinkdiscordadmin")
				.requires((commandSource) -> commandSource.hasPermission(2))
						.then(Commands.argument("playerName", MessageArgument.message())
								.executes(CommandDiscordUnlinkOther::unlinkPlayerName)
				);
		
		dispatcher.register(discordLinkCommand);
	}
	
	static int unlinkPlayerName(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		String playerName = MessageArgument.getMessage(commandContext, "playerName").getString();
		LinkedAccount account = AccountManager.getLinkedAccountFromMinecraftName(playerName);
		if(account != null)
		{
			AccountManager.unlinkAccount(account);
			commandContext.getSource().sendSuccess(new TextComponent(playerName + " is no longer linked to their discord account."), true);
			return 1;
		}
		PartialLinkedAccount partialAccount = AccountManager.getPartialLinkedAccountFromPlayerName(playerName);
		if(partialAccount != null)
		{
			AccountManager.getPartiallyLinkedAccounts().remove(partialAccount);
			AccountManager.markAsDirty();
			commandContext.getSource().sendSuccess(new TextComponent(playerName + " is no longer linked to their discord account."), true);
			return 1;
		}
		else
		{
			commandContext.getSource().sendFailure(new TextComponent("Their account is not linked to a discord account."));
		}
		return 0;
	}
	
}
