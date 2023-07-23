package io.github.lightman314.lightmansdiscord.discord.listeners.account.commands;

import java.util.List;

import io.github.lightman314.lightmansdiscord.api.jda.data.SafeGuildReference;
import io.github.lightman314.lightmansdiscord.discord.links.AccountManager;
import io.github.lightman314.lightmansdiscord.discord.links.LinkedAccount;
import io.github.lightman314.lightmansdiscord.discord.listeners.account.AccountCommand;
import io.github.lightman314.lightmansdiscord.message.MessageManager;

public class DiscordNameCommand extends AccountCommand{

	public DiscordNameCommand() { super("discordname", false, false); }

	@Override
	public void addToHelpText(List<String> output, String prefix) {
		output.add(prefix + this.literal + " <IGN> - " + MessageManager.M_HELP_DISCORDNAME.get());
	}

	@Override
	public void runCommand(String input, LinkedAccount account, SafeGuildReference guild, List<String> output) {
		
		String minecraftName = input.substring(this.literal.length()).replace(" ", "");
		LinkedAccount la = AccountManager.getLinkedAccountFromMinecraftName(minecraftName);
		if(la != null)
		{
			output.add(MessageManager.M_DISCORDNAME_SUCCESS.format(la.getName(), la.getMemberName()));
		}
		else
			output.add(MessageManager.M_DISCORDNAME_FAIL.format(minecraftName));

	}

}
