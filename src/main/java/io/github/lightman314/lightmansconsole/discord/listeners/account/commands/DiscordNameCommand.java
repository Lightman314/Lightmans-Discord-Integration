package io.github.lightman314.lightmansconsole.discord.listeners.account.commands;

import java.util.List;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.listeners.account.AccountCommand;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import net.dv8tion.jda.api.entities.Guild;

public class DiscordNameCommand extends AccountCommand{

	public DiscordNameCommand() {
		super("discordname", false, false);
	}

	@Override
	public void addToHelpText(List<String> output, String prefix) {
		output.add(prefix + this.literal + " <IGN> - Get the discord members name of the given minecraft account.");
	}

	@Override
	public List<String> runCommand(String input, LinkedAccount account, Guild guild, List<String> output) {
		
		String minecraftName = input.substring(this.literal.length()).replace(" ", "");
		LinkedAccount la = AccountManager.getLinkedAccountFromMinecraftName(minecraftName);
		if(la != null)
		{
			output.add(MessageManager.M_DISCORDNAME_SUCCESS.format(la.getName(), la.getMemberName()));
		}
		else
			output.add(MessageManager.M_DISCORDNAME_SUCCESS.format(minecraftName));
		
		return output;
	}

}
