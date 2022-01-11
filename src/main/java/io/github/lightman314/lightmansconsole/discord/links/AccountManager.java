package io.github.lightman314.lightmansconsole.discord.links;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;

import io.github.lightman314.lightmansconsole.LightmansConsole;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class AccountManager extends WorldSavedData{

	private static final String DATA_NAME = LightmansConsole.MODID + "_linked_accounts";
	
	List<PendingLink> pendingLinks = new ArrayList<>();
	public static List<PendingLink> getPendingLinks() { return get().pendingLinks; }
	List<LinkedAccount> linkedAccounts = new ArrayList<>();
	public static List<LinkedAccount> getLinkedAccounts() { return get().linkedAccounts; }
	
	List<String> currencyNotifications = new ArrayList<>();
	
	public AccountManager() {
		super(DATA_NAME);
	}

	public static void markAsDirty()
	{
		get().markDirty();
	}
	
	@Override
	public void read(CompoundNBT compound) {
		
		if(compound.contains("LinkedAccounts", Constants.NBT.TAG_LIST))
		{
			this.linkedAccounts.clear();
			ListNBT accountList = compound.getList("LinkedAccounts", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < accountList.size(); i++)
			{
				CompoundNBT thisCompound = accountList.getCompound(i);
				UUID id = thisCompound.getUniqueId("id");
				String discordID = thisCompound.getString("discord");
				this.linkedAccounts.add(new LinkedAccount(id, discordID));
			}
		}
		if(compound.contains("PendingLinks", Constants.NBT.TAG_LIST))
		{
			this.pendingLinks.clear();
			ListNBT pendingLinkList = compound.getList("PendingLinks", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < pendingLinkList.size(); i++)
			{
				CompoundNBT thisCompound = pendingLinkList.getCompound(i);
				String id = thisCompound.getString("id");
				String linkKey = thisCompound.getString("key");
				this.pendingLinks.add(new PendingLink(linkKey, id));
			}
		}
		if(compound.contains("CurrencyNotifications", Constants.NBT.TAG_LIST))
		{
			this.currencyNotifications.clear();
			ListNBT currencyNotificationList = compound.getList("CurrencyNotifications", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < currencyNotificationList.size(); i++)
			{
				CompoundNBT thisCompound = currencyNotificationList.getCompound(i);
				this.currencyNotifications.add(thisCompound.getString("id"));
			}
		}
		
		if(compound.contains("PartialLinks", Constants.NBT.TAG_LIST))
		{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			ListNBT partialLinkList = compound.getList("PartialLinks", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < partialLinkList.size(); i++)
			{
				CompoundNBT thisCompound = partialLinkList.getCompound(i);
				String name = thisCompound.getString("name");
				String id = thisCompound.getString("id");
				GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(name);
				if(profile != null)
				{
					LinkedAccount newLink = new LinkedAccount(profile.getId(), id);
					if(this.getAccountFromPlayerID(profile.getId()) != null && this.getAccountFromDiscordID(id) != null)
						this.linkedAccounts.add(newLink);
				}
			}
		}
		
		
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		
		ListNBT accountList = new ListNBT();
		for(int i = 0; i < linkedAccounts.size(); i++)
		{
			CompoundNBT thisCompound = new CompoundNBT();
			LinkedAccount thisAccount = linkedAccounts.get(i);
			thisCompound.putUniqueId("id", thisAccount.playerID);
			//thisCompound.putString("name", thisAccount.getName());
			thisCompound.putString("discord", thisAccount.discordID);
			accountList.add(thisCompound);
		}
		compound.put("LinkedAccounts", accountList);
		
		ListNBT pendingLinkList = new ListNBT();
		for(int i = 0; i < this.pendingLinks.size(); i++)
		{
			CompoundNBT thisCompound = new CompoundNBT();
			PendingLink thisLink = pendingLinks.get(i);
			thisCompound.putString("id", thisLink.userID);
			thisCompound.putString("key", thisLink.linkKey);
			pendingLinkList.add(thisCompound);
		}
		compound.put("PendingLinks", pendingLinkList);
		
		ListNBT currencyNotificationList = new ListNBT();
		for(int i = 0; i < this.currencyNotifications.size(); i++)
		{
			CompoundNBT thisCompound = new CompoundNBT();
			thisCompound.putString("id", this.currencyNotifications.get(i));
			currencyNotificationList.add(thisCompound);
		}
		compound.put("CurrencyNotifications", currencyNotificationList);
		
		return compound;
	}
	
	//Non-static functions
	private LinkedAccount getAccountFromMinecraftName(String name)
	{
		for(int i = 0; i < this.linkedAccounts.size(); i++)
		{
			if(this.linkedAccounts.get(i).equalsPlayerName(name))
				return this.linkedAccounts.get(i);
		}
		return null;
	}
	
	public static LinkedAccount getLinkedAccountFromMinecraftName(String name)
	{
		return get().getAccountFromMinecraftName(name);
	}
	
	private LinkedAccount getAccountFromDiscordID(String discordID)
	{
		for(int i = 0; i < this.linkedAccounts.size(); i++)
		{
			if(this.linkedAccounts.get(i).equalsDiscordID(discordID))
				return this.linkedAccounts.get(i);
		}
		return null;
	}
	
	public static LinkedAccount getLinkedAccountFromDiscordID(String discordID)
	{
		return get().getAccountFromDiscordID(discordID);
	}
	
	public static LinkedAccount getLinkedAccountFromUser(User user)
	{
		return get().getAccountFromDiscordID(user.getId());
	}
	
	public static LinkedAccount getLinkedAccountFromMember(Member member)
	{
		return getLinkedAccountFromUser(member.getUser());
	}
	
	private LinkedAccount getAccountFromPlayer(PlayerEntity player)
	{
		for(int i = 0; i < this.linkedAccounts.size(); i++)
		{
			if(this.linkedAccounts.get(i).equalsPlayer(player))
				return this.linkedAccounts.get(i);
		}
		return null;
	}
	
	public static LinkedAccount getLinkedAccountFromPlayer(PlayerEntity player)
	{
		return get().getAccountFromPlayer(player);
	}
	
	private LinkedAccount getAccountFromPlayerID(UUID playerID)
	{
		for(int i = 0; i < this.linkedAccounts.size(); i++)
		{
			if(this.linkedAccounts.get(i).playerID.equals(playerID))
				return this.linkedAccounts.get(i);
		}
		return null;
	}
	
	public static LinkedAccount getLinkedAccountFromPlayerID(UUID playerID)
	{
		return get().getAccountFromPlayerID(playerID);
	}
	
	private void removeLinkedAccount(LinkedAccount account)
	{
		if(this.linkedAccounts.contains(account))
		{
			this.linkedAccounts.remove(account);
			this.markDirty();
		}
	}
	
	private PendingLink getPendingLinkFromUser(User user)
	{
		for(int i = 0; i < this.pendingLinks.size(); i++)
		{
			if(this.pendingLinks.get(i).userID.equals(user.getId()))
				return this.pendingLinks.get(i);
		}
		return null;
	}
	
	private PendingLink getPendingLinkFromKey(String linkKey)
	{
		for(int i = 0; i < this.pendingLinks.size(); i++)
		{
			if(this.pendingLinks.get(i).linkKey.equals(linkKey))
				return this.pendingLinks.get(i);
		}
		return null;
	}
	
	private void removePendingLink(PendingLink link)
	{
		if(this.pendingLinks.contains(link))
		{
			this.pendingLinks.remove(link);
			this.markDirty();
		}
	}
	
	//Static functions
	private static AccountManager get()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		ServerWorld world = server.getWorld(World.OVERWORLD);
		return world.getSavedData().getOrCreate(AccountManager::new, DATA_NAME);
	}
	
	/**
	 * @return The pending link linked to this player
	 */
	public static PendingLink createPendingLink(User user)
	{
		AccountManager manager = get();
		if(manager.getAccountFromDiscordID(user.getId()) != null)//Don't create a pending link if the user is already linked
			return null;
		PendingLink link = manager.getPendingLinkFromUser(user);
		if(link != null)
			return link;
		link = new PendingLink(user.getId());
		manager.pendingLinks.add(link);
		manager.markDirty();
		return link;
	}
	
	public static void unlinkAccount(LinkedAccount account)
	{
		AccountManager manager = get();
		manager.removeLinkedAccount(account);
		manager.markDirty();
	}
	
	/**
	 * @return 1: Success
	 * 0: Invalid link key
	 * -1: Player already linked
	 */
	public static int tryLinkUser(PlayerEntity player, String linkKey)
	{
		AccountManager manager = get();
		PendingLink pendingLink = manager.getPendingLinkFromKey(linkKey);
		if(pendingLink == null)
			return 0;
		if(manager.getAccountFromPlayer(player) != null)
			return -1;
		LinkedAccount newAccount = new LinkedAccount(player.getUniqueID(), pendingLink.userID);
		manager.removePendingLink(pendingLink);
		manager.linkedAccounts.add(newAccount);
		
		return 1;
	}
	
	public static List<String> tryUnlinkUser(User user)
	{
		AccountManager manager = get();
		String discordID = user.getId();
		LinkedAccount account = manager.getAccountFromDiscordID(discordID);
		if(account != null)
		{
			manager.removeLinkedAccount(account);
			return ImmutableList.of("Your discord account has been successfully unlinked from '" + account.getName() + "'.");
		}
		
		PendingLink pendingLink = manager.getPendingLinkFromUser(user);
		if(pendingLink != null)
		{
			manager.removePendingLink(pendingLink);
			return ImmutableList.of("You discord accounts pending link has been removed.");
		}
		
		return ImmutableList.of("Your discord account is not linked to a minecraft account on this server.");
	}
	
	public static List<String> tryForceUnlinkUser(JDA jda, String playerName)
	{
		AccountManager manager = get();
		LinkedAccount account = manager.getAccountFromMinecraftName(playerName);
		if(account != null)
		{
			manager.removeLinkedAccount(account);
			AtomicReference<User> currentLinkedUser = new AtomicReference<>();
			String currentLinkedDiscordID = account.discordID;
			jda.getUsers().forEach(u ->{
				if(u.getId().equals(currentLinkedDiscordID))
					currentLinkedUser.set(u);
			});
			if(currentLinkedUser.get() != null)
				return ImmutableList.of("'" + playerName + "' has been unlinked from " + currentLinkedUser.get().getName() + "'s account.");
			else
				return ImmutableList.of("'" + playerName + "' has been unlinked from the unknown users account.");
		}
		
		return ImmutableList.of("'" + playerName + "' is not linked to any accounts.");
	}
	
	public static List<String> tryLinkUser2(User user, String playerName)
	{
		AccountManager manager = get();
		if(manager.getAccountFromMinecraftName(playerName) != null)
			return ImmutableList.of("'" + playerName + "' is already linked to a discord account.");
		if(manager.getAccountFromDiscordID(user.getId()) != null)
			return ImmutableList.of("'" + user.getName() + "' is already linked to a minecraft account.");
		if(manager.getPendingLinkFromUser(user) != null)
			return ImmutableList.of("'" + user.getName() + "' already has a pending link.");
		
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		GameProfile playerProfile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
		if(playerProfile != null)
		{
			LinkedAccount newAccount = new LinkedAccount(playerProfile.getId(), user.getId());
			manager.linkedAccounts.add(newAccount);
			manager.markDirty();
			LightmansConsole.LOGGER.info("Linked discord #" + user.getId() + " with '" + playerProfile.getName() + "' (" + playerProfile.getId().toString() + ")");
			return ImmutableList.of("Successfully linked " + user.getName() + " to '" + playerProfile.getName() + "'");
		}
		else
			return ImmutableList.of(playerName + " is not a valid Minecraft account.");
		
	}

	//Currency notification functions
	public static boolean currencyNotificationsEnabled(User user)
	{
		if(user == null)
			return false;
		return get().currencyNotifications.contains(user.getId());
	}
	
	public static boolean enableCurrencyNotifications(User user)
	{
		List<String> currencyNotifications = get().currencyNotifications;
		if(!currencyNotifications.contains(user.getId()))
		{
			currencyNotifications.add(user.getId());
			get().markDirty();
			return true;
		}
		return false;
	}
	
	public static boolean disableCurrencyNotifications(User user)
	{
		List<String> currencyNotifications = get().currencyNotifications;
		if(currencyNotifications.contains(user.getId()))
		{
			currencyNotifications.remove(user.getId());
			get().markDirty();
			return true;
		}
		return false;
	}
	
}
