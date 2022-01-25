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
import net.minecraft.entity.player.PlayerEntity;

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
			commandContext.getSource().sendFeedback(MessageManager.M_COMMAND_UNLINK_COMPLETE.formatComponent(account.getMemberName()), true);
			return 1;
		}
		else
		{
			commandContext.getSource().sendErrorMessage(MessageManager.M_COMMAND_UNLINK_FAILED.formatComponent());
		}
		return 0;
	}
	
}
