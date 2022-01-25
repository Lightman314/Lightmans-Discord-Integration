package io.github.lightman314.lightmansconsole.commands;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.links.PendingLink;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class CommandDiscordList {
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> discordLinkCommand
			= Commands.literal("discordlist")
				.requires((commandSource) -> commandSource.hasPermissionLevel(2))
				.executes(CommandDiscordList::listLinks);
		
		dispatcher.register(discordLinkCommand);
	}
	
	static int listLinks(CommandContext<CommandSource> commandContext) throws CommandSyntaxException{
		
		CommandSource source = commandContext.getSource();
		//List linked accounts
		List<LinkedAccount> a = AccountManager.getLinkedAccounts();
		if(a.size() > 0)
		{
			source.sendFeedback(new StringTextComponent("--------Linked Accounts--------"), false);
			a.forEach(account-> source.sendFeedback(new StringTextComponent("DiscordID: " + account.discordID + "; PlayerID: " + account.playerID + "; PlayerName: " + account.getName()), false));
		}
		
		//List pending links
		List<PendingLink> pend = AccountManager.getPendingLinks();
		if(pend.size() >= 0)
		{
			source.sendFeedback(new StringTextComponent("--------Pending Links--------"), false);
			pend.forEach(account -> source.sendFeedback(new StringTextComponent("DiscordID: " + account.userID + "; LinkKey: " + account.linkKey), false));
		}
		
		if(a.size() <= 0 && pend.size() <= 0)
		{
			source.sendFeedback(new StringTextComponent("No discord accounts are linked to this server."), false);
		}
		
		return 1;
		
	}
	
}
