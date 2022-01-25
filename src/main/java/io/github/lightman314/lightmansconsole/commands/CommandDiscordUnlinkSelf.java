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
import net.minecraft.world.entity.player.Player;

public class CommandDiscordUnlinkSelf {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> discordLinkCommand
			= Commands.literal("unlinkdiscord")
				.requires((commandSource) -> commandSource.getEntity() instanceof Player)
				.executes(CommandDiscordUnlinkSelf::unlinkPlayer);
		
		dispatcher.register(discordLinkCommand);
	}
	
	static int unlinkPlayer(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		Player player = commandContext.getSource().getPlayerOrException();
		LinkedAccount account = AccountManager.getLinkedAccountFromPlayer(player);
		if(account != null)
		{
			AccountManager.unlinkAccount(account);
			commandContext.getSource().sendSuccess(MessageManager.M_COMMAND_UNLINK_COMPLETE.formatComponent(account.getMemberName()), true);
			return 1;
		}
		else
		{
			commandContext.getSource().sendFailure(MessageManager.M_COMMAND_UNLINK_FAILED.formatComponent());
		}
		return 0;
	}
	
}
