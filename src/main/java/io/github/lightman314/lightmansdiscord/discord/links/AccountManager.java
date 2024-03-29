package io.github.lightman314.lightmansdiscord.discord.links;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;

import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeMemberReference;
import io.github.lightman314.lightmansdiscord.api.jda.data.SafeUserReference;
import io.github.lightman314.lightmansdiscord.message.MessageManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber
public class AccountManager extends SavedData{

	private static final String DATA_NAME = "lightmansconsole_linked_accounts";
	
	List<PendingLink> pendingLinks = new ArrayList<>();
	public static List<PendingLink> getPendingLinks() { return get().pendingLinks; }
	List<LinkedAccount> linkedAccounts = new ArrayList<>();
	public static List<LinkedAccount> getLinkedAccounts() { return get().linkedAccounts; }
	
	List<String> currencyNotifications = new ArrayList<>();
	
	public AccountManager() { }
	
	public AccountManager(CompoundTag compound)
	{

		if(compound.contains("LinkedAccounts", Tag.TAG_LIST))
		{
			this.linkedAccounts.clear();
			ListTag accountList = compound.getList("LinkedAccounts", Tag.TAG_COMPOUND);
			for(int i = 0; i < accountList.size(); i++)
			{
				CompoundTag thisCompound = accountList.getCompound(i);
				UUID id = thisCompound.getUUID("id");
				String discordID = thisCompound.getString("discord");
				this.linkedAccounts.add(new LinkedAccount(id, discordID));
			}
		}
		if(compound.contains("PendingLinks", Tag.TAG_LIST))
		{
			this.pendingLinks.clear();
			ListTag pendingLinkList = compound.getList("PendingLinks", Tag.TAG_COMPOUND);
			for(int i = 0; i < pendingLinkList.size(); i++)
			{
				CompoundTag thisCompound = pendingLinkList.getCompound(i);
				String id = thisCompound.getString("id");
				String linkKey = thisCompound.getString("key");
				this.pendingLinks.add(new PendingLink(linkKey, id));
			}
		}
		if(compound.contains("CurrencyNotifications", Tag.TAG_LIST))
		{
			this.currencyNotifications.clear();
			ListTag currencyNotificationList = compound.getList("CurrencyNotifications", Tag.TAG_COMPOUND);
			for(int i = 0; i < currencyNotificationList.size(); i++)
			{
				CompoundTag thisCompound = currencyNotificationList.getCompound(i);
				this.currencyNotifications.add(thisCompound.getString("id"));
			}
		}

		//Convert partial links into full links
		if(compound.contains("PartialLinks", Tag.TAG_LIST))
		{
			ListTag partialLinkList = compound.getList("PartialLinks", Tag.TAG_COMPOUND);
			{
				MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
				for(int i = 0; i < partialLinkList.size(); ++i)
				{
					CompoundTag thisCompound = partialLinkList.getCompound(i);
					String name = thisCompound.getString("name");
					String id = thisCompound.getString("id");
					GameProfile profile = server.getProfileCache().get(name).orElse(null);
					if(profile != null)
					{
						LinkedAccount newLink = new LinkedAccount(profile.getId(), id);
						//Confirm that a linked account doesn't already exist for that player/user
						if(this.getAccountFromPlayerID(profile.getId()) == null && this.getAccountFromDiscordID(id) == null)
							this.linkedAccounts.add(newLink);
					}
				}
			}
		}
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag compound) {
		
		ListTag accountList = new ListTag();
		for (LinkedAccount thisAccount : linkedAccounts) {
			CompoundTag thisCompound = new CompoundTag();
			thisCompound.putUUID("id", thisAccount.playerID);
			//thisCompound.putString("name", thisAccount.getName());
			thisCompound.putString("discord", thisAccount.discordID);
			accountList.add(thisCompound);
		}
		compound.put("LinkedAccounts", accountList);
		
		ListTag pendingLinkList = new ListTag();
		for (PendingLink thisLink : this.pendingLinks) {
			CompoundTag thisCompound = new CompoundTag();
			thisCompound.putString("id", thisLink.userID);
			thisCompound.putString("key", thisLink.linkKey);
			pendingLinkList.add(thisCompound);
		}
		compound.put("PendingLinks", pendingLinkList);
		
		ListTag currencyNotificationList = new ListTag();
		for (String currencyNotification : this.currencyNotifications) {
			CompoundTag thisCompound = new CompoundTag();
			thisCompound.putString("id", currencyNotification);
			currencyNotificationList.add(thisCompound);
		}
		compound.put("CurrencyNotifications", currencyNotificationList);
		
		return compound;
	}
	
	//Non-static functions
	private LinkedAccount getAccountFromMinecraftName(String name)
	{
		for (LinkedAccount linkedAccount : this.linkedAccounts) {
			if (linkedAccount.equalsPlayerName(name))
				return linkedAccount;
		}
		return null;
	}
	
	public static LinkedAccount getLinkedAccountFromMinecraftName(String name)
	{
		return get().getAccountFromMinecraftName(name);
	}
	
	private LinkedAccount getAccountFromDiscordID(String discordID)
	{
		for (LinkedAccount linkedAccount : this.linkedAccounts) {
			if (linkedAccount.equalsDiscordID(discordID))
				return linkedAccount;
		}
		return null;
	}
	
	public static LinkedAccount getLinkedAccountFromDiscordID(String discordID) { return get().getAccountFromDiscordID(discordID); }

	@Deprecated(since = "0.2.0.0")
	public static LinkedAccount getLinkedAccountFromUser(User user) { return getLinkedAccountFromDiscordID(user.getId()); }
	public static LinkedAccount getLinkedAccountFromUser(SafeUserReference user) { return getLinkedAccountFromDiscordID(user.getID()); }

	@Deprecated(since = "0.2.0.0")
	public static LinkedAccount getLinkedAccountFromMember(Member member) { return getLinkedAccountFromUser(member.getUser()); }
	public static LinkedAccount getLinkedAccountFromMember(SafeMemberReference member) { return getLinkedAccountFromUser(member); }

	private LinkedAccount getAccountFromPlayer(Player player)
	{
		for (LinkedAccount linkedAccount : this.linkedAccounts) {
			if (linkedAccount.equalsPlayer(player))
				return linkedAccount;
		}
		return null;
	}
	
	public static LinkedAccount getLinkedAccountFromPlayer(Player player)
	{
		return get().getAccountFromPlayer(player);
	}
	
	private LinkedAccount getAccountFromPlayerID(UUID playerID)
	{
		for (LinkedAccount linkedAccount : this.linkedAccounts) {
			if (linkedAccount.playerID.equals(playerID))
				return linkedAccount;
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
			this.setDirty();
		}
	}
	
	private PendingLink getPendingLinkFromUser(User user)
	{
		for (PendingLink pendingLink : this.pendingLinks) {
			if (pendingLink.userID.equals(user.getId()))
				return pendingLink;
		}
		return null;
	}
	
	private PendingLink getPendingLinkFromKey(String linkKey)
	{
		for (PendingLink pendingLink : this.pendingLinks) {
			if (pendingLink.linkKey.equals(linkKey))
				return pendingLink;
		}
		return null;
	}
	
	private void removePendingLink(PendingLink link)
	{
		if(this.pendingLinks.contains(link))
		{
			this.pendingLinks.remove(link);
			this.setDirty();
		}
	}
	
	//Static functions
	private static AccountManager get()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		ServerLevel world = server.overworld();
		return world.getDataStorage().computeIfAbsent(AccountManager::new, AccountManager::new, DATA_NAME);
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
		manager.setDirty();
		return link;
	}
	
	public static void unlinkAccount(LinkedAccount account)
	{
		AccountManager manager = get();
		manager.removeLinkedAccount(account);
		manager.setDirty();
	}
	
	/**
	 * @return 1: Success
	 * 0: Invalid link key
	 * -1: Player already linked
	 */
	public static int tryLinkUser(Player player, String linkKey)
	{
		AccountManager manager = get();
		PendingLink pendingLink = manager.getPendingLinkFromKey(linkKey);
		if(pendingLink == null)
			return 0;
		if(manager.getAccountFromPlayer(player) != null)
			return -1;
		LinkedAccount newAccount = new LinkedAccount(player.getUUID(), pendingLink.userID);
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
			return ImmutableList.of(MessageManager.M_UNLINK_SUCCESS.format(account.getName()));
		}
		
		PendingLink pendingLink = manager.getPendingLinkFromUser(user);
		if(pendingLink != null)
		{
			manager.removePendingLink(pendingLink);
			return ImmutableList.of(MessageManager.M_UNLINK_PENDING.get());
		}
		
		return ImmutableList.of(MessageManager.M_UNLINK_FAIL.get());
	}
	
	public static List<String> tryForceUnlinkUser(String playerName)
	{
		AccountManager manager = get();
		LinkedAccount account = manager.getAccountFromMinecraftName(playerName);
		if(account != null)
		{
			manager.removeLinkedAccount(account);
			return ImmutableList.of(MessageManager.M_UNLINKPLAYER_SUCCESS.format(account.getName(), account.getMemberName()));
		}
		
		return ImmutableList.of(MessageManager.M_UNLINKPLAYER_FAIL.format(playerName));
	}
	
	public static List<String> tryLinkUser2(User user, String playerName)
	{
		AccountManager manager = get();
		if(manager.getAccountFromMinecraftName(playerName) != null)
			return ImmutableList.of(MessageManager.M_LINKUSER_PLAYERLINKED.format(playerName));
		if(manager.getAccountFromDiscordID(user.getId()) != null)
			return ImmutableList.of(MessageManager.M_LINKUSER_USERLINKED.format(user.getName(), manager.getAccountFromDiscordID(user.getId()).getName()));
		if(manager.getPendingLinkFromUser(user) != null)
			return ImmutableList.of(MessageManager.M_LINKUSER_USERPENDING.format(user.getName()));
		
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		GameProfile playerProfile = server.getProfileCache().get(playerName).orElse(null);
		if(playerProfile != null)
		{
			LinkedAccount newAccount = new LinkedAccount(playerProfile.getId(), user.getId());
			manager.linkedAccounts.add(newAccount);
			manager.setDirty();
			LightmansDiscordIntegration.LOGGER.info("Linked discord #" + user.getId() + " with '" + playerProfile.getName() + "' (" + playerProfile.getId().toString() + ")");
			return ImmutableList.of(MessageManager.M_LINKUSER_SUCCESS.format(user.getName(), newAccount.getName()));
		}
		else
			return ImmutableList.of(MessageManager.M_LINKUSER_NO_ACCOUNT.format(playerName));
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
			get().setDirty();
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
			get().setDirty();
			return true;
		}
		return false;
	}
	
}
