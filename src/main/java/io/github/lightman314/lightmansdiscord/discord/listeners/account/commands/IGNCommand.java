package io.github.lightman314.lightmansdiscord.discord.listeners.account.commands;

import java.util.List;

import io.github.lightman314.lightmansdiscord.api.jda.data.SafeGuildReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeMemberReference;
import io.github.lightman314.lightmansdiscord.discord.links.AccountManager;
import io.github.lightman314.lightmansdiscord.discord.links.LinkedAccount;
import io.github.lightman314.lightmansdiscord.discord.listeners.account.AccountCommand;
import io.github.lightman314.lightmansdiscord.message.MessageManager;
import io.github.lightman314.lightmansdiscord.util.MemberUtil;

public class IGNCommand extends AccountCommand{

	public IGNCommand() {
		super("ign", false, false);
	}

	@Override
	public void addToHelpText(List<String> output, String prefix) {
		output.add(prefix + this.literal + " @user - " + MessageManager.M_HELP_IGN.get());
	}

	@Override
	public void runCommand(String input, LinkedAccount account, SafeGuildReference guild, List<String> output) {
		
		String subcommand = input.substring(this.literal.length());
		SafeMemberReference member = MemberUtil.getMemberFromPing(guild, subcommand);
		if(member != null)
		{
			LinkedAccount la = AccountManager.getLinkedAccountFromMember(member);
			if(la != null)
			{
				output.add(MessageManager.M_IGN_SUCCESS.format(member.getEffectiveName(), la.getName()));
			}
			else
			{
				output.add(this.accountNotLinkedErrorForMember(member));
			}
		}
		else
		{
			output.add(this.cannotGetUserFromPingError());
		}
	}
	
	

}
