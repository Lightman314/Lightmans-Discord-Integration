package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class CommandDiscordUnlinkSelf {

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> discordLinkCommand
			= Commands.literal("unlinkdiscord")
				.requires((commandSource) -> commandSource.getEntity() instanceof PlayerEntity)
				.executes(CommandDiscordUnlinkSelf::unlinkPlayer);
		
		dispatcher.register(discordLinkCommand);
	}
	
	static int unlinkPlayer(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		PlayerEntity player = commandContext.getSource().asPlayer();
		LinkedAccount account = AccountManager.getLinkedAccountFromPlayer(player);
		if(account != null)
		{
			AccountManager.unlinkAccount(account);
			commandContext.getSource().sendFeedback(new StringTextComponent(player.getName().getString() + " is no longer linked to your discord account."), true);
			return 1;
		}
		else
		{
			commandContext.getSource().sendErrorMessage(new StringTextComponent("Your account is not linked to a discord account."));
		}
		return 0;
	}
	
}
