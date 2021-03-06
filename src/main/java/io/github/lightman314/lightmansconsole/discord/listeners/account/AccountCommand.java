package io.github.lightman314.lightmansconsole.discord.listeners.account;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public abstract class AccountCommand {

	public final String literal;
	public final boolean requiresLinkedAccount;
	private final boolean requiresAdmin;
	public final boolean canRun(boolean isAdmin) { return isAdmin || !requiresAdmin; }
	private Supplier<JDA> jdaSource;
	private Supplier<String> prefixSource;
	
	protected AccountCommand(String literal, boolean requiresLinkedAccount, boolean requiresAdmin)
	{
		this.literal = literal;
		this.requiresLinkedAccount = requiresLinkedAccount;
		this.requiresAdmin = requiresAdmin;
	}
	
	public void setup(Supplier<JDA> jdaSource, Supplier<String> prefixSource)
	{
		this.jdaSource = jdaSource;
		this.prefixSource = prefixSource;
	}
	
	protected final JDA getJDA() { return jdaSource.get(); }
	protected final String getPrefix() { return prefixSource.get(); }
	
	public abstract void addToHelpText(List<String> output, String prefix);
	
	public abstract List<String> runCommand(String input, @Nullable LinkedAccount account, Guild guild, List<String> output);
	
	public String accountNotLinkedErrorSelf()
	{
		return MessageManager.M_ERROR_NOTLINKEDSELF.get();
	}
	
	public String accountNotLinkedErrorForUser(User user)
	{
		return MessageManager.M_ERROR_NOTLINKED.format(user.getName());
	}
	
	public String accountNotLinkedErrorForMember(Member member)
	{
		return MessageManager.M_ERROR_NOTLINKED.format(member.getEffectiveName());
	}
	
	public String cannotGetUserFromPingError()
	{
		return MessageManager.M_ERROR_PING.get();
	}
	
}
