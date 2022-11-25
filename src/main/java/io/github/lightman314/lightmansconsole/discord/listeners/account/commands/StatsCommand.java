package io.github.lightman314.lightmansconsole.discord.listeners.account.commands;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.listeners.account.AccountCommand;
import io.github.lightman314.lightmansconsole.util.MemberUtil;
import io.github.lightman314.lightmansconsole.util.PlayerUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

public class StatsCommand extends AccountCommand{
	
	public StatsCommand() { super("stats", false, false); }
	
	@Override
	public void addToHelpText(List<String> output, String prefix)
	{
		output.add(prefix + this.literal + " @discorduser [statFilter] - Get another users minecraft stats.");
		output.add(prefix + this.literal + " [statFilter] - Get your minecraft stats.");
	}
	
	@Override
	public void runCommand(String commandInput, LinkedAccount account, Guild guild, List<String> output) {
		
		if(commandInput.length() > this.literal.length())
		{
			String commandContext = commandInput.substring(this.literal.length() + 1);
			if(commandContext.startsWith("<@"))
			{
				//Second input is a ping, so we're getting stats for another user
				Member member = MemberUtil.getMemberFromPing(guild, commandContext);
				if(member != null)
				{
					LinkedAccount a = AccountManager.getLinkedAccountFromMember(member);
					if(a != null)
					{
						//Type defined
						String type = commandContext.substring(commandContext.indexOf('>') + 2).toLowerCase();
						this.getStatsForPlayerID(a.playerID, type, output);
					}
					else
						output.add(this.accountNotLinkedErrorForMember(member));
				}
				else
				{
					output.add(this.cannotGetUserFromPingError());
				}
			}
			else if(account != null)
			{
				//No user defined, so run for self with defined type
				this.getStatsForPlayerID(account.playerID, commandContext.toLowerCase(), output);
			}
			else
				output.add(this.accountNotLinkedErrorSelf());
		}
		else if(account != null)
		{
			//No input, so self + all types
			this.getStatsForPlayerID(account.playerID, "", output);
		}
		else //Throw an error for self if no account was given
			output.add(this.accountNotLinkedErrorSelf());
	}
	
	private void getStatsForPlayerID(UUID playerId, String type, List<String> output)
	{
		int outputStartLength = output.size();
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		GameProfile profile = PlayerUtil.playerProfile(playerId);
		if(profile != null && server != null)
		{
			Player fakePlayer = new FakePlayer(server.overworld(), profile);
			fakePlayer.remove(RemovalReason.DISCARDED);
			ServerStatsCounter statsManager = server.getPlayerList().getPlayerStats(fakePlayer);
			AtomicReference<String> typeName = new AtomicReference<>(type.toLowerCase());
			this.getAllStats().forEach(thisStat ->{
				if(thisStat.getName().toLowerCase().contains(typeName.get()))
				{
					int value = statsManager.getValue(thisStat);
					if(value > 0)
						output.add(thisStat.getName() + ": " + value);
				}
			});
		}
		else
			output.add("**ERROR**: Could not get game profile of the linked user!");
		
		if(output.size() == outputStartLength)
		{
			if(profile == null)
			{
				output.add("Could not find player profile for the given player.");
			}
			else
			{
				String error = "No results found for " + profile.getName();
				if(!type.isEmpty())
					error += " for query '" + type + "'";
				output.add(error);
			}
			
		}
			
		
	}
	
	private static List<Stat<?>> ALL_STATS = null;
	
	private List<Stat<?>> getAllStats()
	{
		if(ALL_STATS == null)
		{
			//Get all stat types
			ALL_STATS = Lists.newArrayList();
			ForgeRegistries.STAT_TYPES.forEach(statType ->{
				statType.forEach(stat ->{
					ALL_STATS.add(stat);
				});
			});
		}
		return ALL_STATS;
	}

	
	
}
