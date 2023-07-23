package io.github.lightman314.lightmansdiscord.discord.listeners.account;

import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmansdiscord.api.jda.data.SafeGuildReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeMemberReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeUserReference;
import io.github.lightman314.lightmansdiscord.discord.links.LinkedAccount;
import io.github.lightman314.lightmansdiscord.message.MessageManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public abstract class AccountCommand {

	public final String literal;
	public final boolean requiresLinkedAccount;
	private final boolean requiresAdmin;
	public final boolean canRun(boolean isAdmin) { return isAdmin || !requiresAdmin; }
	
	protected AccountCommand(String literal, boolean requiresLinkedAccount, boolean requiresAdmin)
	{
		this.literal = literal;
		this.requiresLinkedAccount = requiresLinkedAccount;
		this.requiresAdmin = requiresAdmin;
	}
	
	public abstract void addToHelpText(List<String> output, String prefix);

	@Deprecated(since = "0.2.0.0")
	protected void runCommand(String input, @Nullable LinkedAccount account, Guild guild, List<String> output) {}
	protected abstract void runCommand(String input, @Nullable LinkedAccount account, SafeGuildReference guild, List<String> output);

	public void safeRunCommand(String input, @Nullable LinkedAccount account, SafeGuildReference guild, List<String> output)
	{
		try{ this.runCommand(input, account, guild, output);
		}catch (Throwable t) { try{
			this.runCommand(input, account, guild.getGuild(), output);
		} catch (Throwable t2) { t.printStackTrace(); } }
	}

	public String accountNotLinkedErrorSelf()
	{
		return MessageManager.M_ERROR_NOTLINKEDSELF.get();
	}
	@Deprecated(since = "0.2.0.0")
	public String accountNotLinkedErrorForUser(User user) { return MessageManager.M_ERROR_NOTLINKED.format(user.getName()); }
	public String accountNotLinkedErrorForUser(SafeUserReference user) { return MessageManager.M_ERROR_NOTLINKED.format(user.getName()); }

	public String accountNotLinkedErrorForMember(SafeMemberReference member) { return MessageManager.M_ERROR_NOTLINKED.format(member.getEffectiveName()); }
	@Deprecated(since = "0.2.0.0")
	public String accountNotLinkedErrorForMember(Member member) { return MessageManager.M_ERROR_NOTLINKED.format(member.getEffectiveName()); }
	
	public String cannotGetUserFromPingError()
	{
		return MessageManager.M_ERROR_PING.get();
	}
	
}
