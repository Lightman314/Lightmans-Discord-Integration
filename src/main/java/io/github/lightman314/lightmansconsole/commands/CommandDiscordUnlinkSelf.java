package io.github.lightman314.lightmansconsole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
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
			commandContext.getSource().sendSuccess(new TextComponent(player.getName().getString() + " is no longer linked to your discord account."), true);
			return 1;
		}
		else
		{
			commandContext.getSource().sendFailure(new TextComponent("Your account is not linked to a discord account."));
		}
		return 0;
	}
	
}
