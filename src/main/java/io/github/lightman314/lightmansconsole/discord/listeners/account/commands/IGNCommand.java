package io.github.lightman314.lightmansconsole.discord.listeners.account.commands;

import java.util.List;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.listeners.account.AccountCommand;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import io.github.lightman314.lightmansconsole.util.MemberUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class IGNCommand extends AccountCommand{

	public IGNCommand() {
		super("ign", false, false);
	}

	@Override
	public void addToHelpText(List<String> output, String prefix) {
		output.add(prefix + this.literal + " @user - " + MessageManager.M_HELP_IGN.get());
	}

	@Override
	public void runCommand(String input, LinkedAccount account, Guild guild, List<String> output) {
		
		String subcommand = input.substring(this.literal.length());
		Member member = MemberUtil.getMemberFromPing(guild, subcommand);
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
