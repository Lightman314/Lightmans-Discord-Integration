package io.github.lightman314.lightmansconsole.commands;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.links.PendingLink;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CommandDiscordList {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> discordLinkCommand
			= Commands.literal("discordlist")
				.requires((commandSource) -> commandSource.hasPermission(2))
				.executes(CommandDiscordList::listLinks);
		
		dispatcher.register(discordLinkCommand);
	}
	
	static int listLinks(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		//List linked accounts
		List<LinkedAccount> a = AccountManager.getLinkedAccounts();
		if(a.size() > 0)
		{
			source.sendSuccess(Component.literal("--------Linked Accounts--------"), false);
			a.forEach(account-> source.sendSuccess(Component.literal("DiscordID: " + account.discordID + "; PlayerID: " + account.playerID + "; PlayerName: " + account.getName()), false));
		}
		//List pending links
		List<PendingLink> pend = AccountManager.getPendingLinks();
		if(pend.size() > 0)
		{
			source.sendSuccess(Component.literal("--------Pending Links--------"), false);
			pend.forEach(account -> source.sendSuccess(Component.literal("DiscordID: " + account.userID + "; LinkKey: " + account.linkKey), false));
		}
		
		if(a.size() <= 0 && pend.size() <= 0)
		{
			source.sendSuccess(Component.literal("No discord accounts are linked to this server."), false);
		}
		
		return 1;
		
	}
	
}
