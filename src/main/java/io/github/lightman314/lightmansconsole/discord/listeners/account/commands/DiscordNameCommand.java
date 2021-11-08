package io.github.lightman314.lightmansconsole.discord.listeners.account.commands;

import java.util.List;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.links.PartialLinkedAccount;
import io.github.lightman314.lightmansconsole.discord.listeners.account.AccountCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

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
		String discordID = "";
		if(la != null)
			discordID = la.discordID;
		else
		{
			PartialLinkedAccount pl = AccountManager.getPartialLinkedAccountFromPlayerName(minecraftName);
			if(pl != null)
				discordID = pl.discordID;
		}
		if(!discordID.isEmpty())
		{
			Member member = guild.getMemberById(discordID);
			if(member != null)
				output.add("'" + minecraftName + "' is linked to " + member.getEffectiveName());
			else
				output.add("'" + minecraftName + "' is linked, but the member is no longer on this discord server.");
		}
		
		return output;
	}

}
