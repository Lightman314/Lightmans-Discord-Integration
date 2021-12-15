package io.github.lightman314.lightmansconsole.discord.links;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmansconsole.LightmansConsole;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber
public class AccountManager extends SavedData{

	private static final String DATA_NAME = LightmansConsole.MODID + "_linked_accounts";
	
	List<PendingLink> pendingLinks = new ArrayList<>();
	public static List<PendingLink> getPendingLinks() { return get().pendingLinks; }
	List<LinkedAccount> linkedAccounts = new ArrayList<>();
	public static List<LinkedAccount> getLinkedAccounts() { return get().linkedAccounts; }
	List<PartialLinkedAccount> partiallyLinkedAccounts = new ArrayList<>();
	public static List<PartialLinkedAccount> getPartiallyLinkedAccounts() { return get().partiallyLinkedAccounts; }
	
	List<String> currencyNotifications = new ArrayList<>();
	
	public AccountManager() { }
	
	public AccountManager(CompoundTag tag) { this.load(tag); }

	public static void markAsDirty()
	{
		get().setDirty();
	}
	
	public void load(CompoundTag compound) {
		
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
		if(compound.contains("PartialLinks", Tag.TAG_LIST))
		{
			this.partiallyLinkedAccounts.clear();
			ListTag partialLinkList = compound.getList("PartialLinks", Tag.TAG_COMPOUND);
			for(int i = 0; i < partialLinkList.size(); i++)
			{
				CompoundTag thisCompound = partialLinkList.getCompound(i);
				String name = thisCompound.getString("name");
				String id = thisCompound.getString("id");
				this.partiallyLinkedAccounts.add(new PartialLinkedAccount(name, id));
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
		
		
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		
		ListTag accountList = new ListTag();
		for(int i = 0; i < linkedAccounts.size(); i++)
		{
			CompoundTag thisCompound = new CompoundTag();
			LinkedAccount thisAccount = linkedAccounts.get(i);
			thisCompound.putUUID("id", thisAccount.playerID);
			//thisCompound.putString("name", thisAccount.getName());
			thisCompound.putString("discord", thisAccount.discordID);
			accountList.add(thisCompound);
		}
		compound.put("LinkedAccounts", accountList);
		ListTag pendingLinkList = new ListTag();
		for(int i = 0; i < this.pendingLinks.size(); i++)
		{
			CompoundTag thisCompound = new CompoundTag();
			PendingLink thisLink = pendingLinks.get(i);
			thisCompound.putString("id", thisLink.userID);
			thisCompound.putString("key", thisLink.linkKey);
			pendingLinkList.add(thisCompound);
		}
		compound.put("PendingLinks", pendingLinkList);
		ListTag partialLinkList = new ListTag();
		for(int i = 0; i < this.partiallyLinkedAccounts.size(); i++)
		{
			CompoundTag thisCompound = new CompoundTag();
			PartialLinkedAccount thisAccount = partiallyLinkedAccounts.get(i);
			thisCompound.putString("name", thisAccount.playerName);
			thisCompound.putString("id", thisAccount.discordID);
			partialLinkList.add(thisCompound);
		}
		compound.put("PartialLinks", partialLinkList);
		
		ListTag currencyNotificationList = new ListTag();
		for(int i = 0; i < this.currencyNotifications.size(); i++)
		{
			CompoundTag thisCompound = new CompoundTag();
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
	
	private LinkedAccount getAccountFromPlayer(Player player)
	{
		for(int i = 0; i < this.linkedAccounts.size(); i++)
		{
			if(this.linkedAccounts.get(i).equalsPlayer(player))
				return this.linkedAccounts.get(i);
		}
		return null;
	}
	
	public static LinkedAccount getLinkedAccountFromPlayer(Player player)
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
			this.setDirty();
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
			this.setDirty();
		}
	}
	
	public static PartialLinkedAccount getPartialLinkedAccountFromMember(Member member)
	{
		return get().getPartialLinkFromUser(member.getUser());
	}
	
	public static PartialLinkedAccount getPartialLinkedAccountFromUser(User user)
	{
		return get().getPartialLinkFromUser(user);
	}
	
	private PartialLinkedAccount getPartialLinkFromUser(User user)
	{
		for(int i = 0; i < this.partiallyLinkedAccounts.size(); i++)
		{
			if(this.partiallyLinkedAccounts.get(i).discordID.equals(user.getId()))
				return this.partiallyLinkedAccounts.get(i);
		}
		return null;
	}
	
	public static PartialLinkedAccount getPartialLinkedAccountFromPlayerName(String playerName)
	{
		return get().getPartialLinkFromName(playerName);
	}
	
	private PartialLinkedAccount getPartialLinkFromName(String playerName)
	{
		for(int i = 0; i < this.partiallyLinkedAccounts.size(); i++)
		{
			if(this.partiallyLinkedAccounts.get(i).playerName.equals(playerName))
				return this.partiallyLinkedAccounts.get(i);
		}
		return null;
	}
	
	//Static functions
	private static AccountManager get()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		ServerLevel world = server.getLevel(Level.OVERWORLD);
		return world.getDataStorage().computeIfAbsent((compound) -> new AccountManager(compound), () -> new AccountManager(), DATA_NAME);
	}
	
	/**
	 * @return The pending link linked to this player
	 */
	public static PendingLink createPendingLink(User user)
	{
		AccountManager manager = get();
		if(manager.getAccountFromDiscordID(user.getId()) != null)//Don't create a pending link if the user is already linked
			return null;
		if(manager.getPartialLinkFromUser(user) != null) //Don't create a pending link if the user is already linked
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
			return ImmutableList.of("Your discord account has been successfully unlinked from '" + account.getName() + "'.");
		}
		
		PartialLinkedAccount partialLink = manager.getPartialLinkFromUser(user);
		if(partialLink != null)
		{
			manager.partiallyLinkedAccounts.remove(partialLink);
			manager.setDirty();
			return ImmutableList.of("Your discord account has been successfully unlinked from '" + partialLink.playerName + "'.");
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
		
		PartialLinkedAccount partialLink = manager.getPartialLinkFromName(playerName);
		if(partialLink != null)
		{
			manager.partiallyLinkedAccounts.remove(partialLink);
			manager.setDirty();
			AtomicReference<User> currentLinkedUser = new AtomicReference<>();
			String currentLinkedDiscordID = partialLink.discordID;
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
	
	public static List<String> tryCreatePartialLink(User user, String playerName)
	{
		AccountManager manager = get();
		if(manager.getAccountFromMinecraftName(playerName) != null || manager.getPartialLinkFromName(playerName) != null)
			return ImmutableList.of("'" + playerName + "' is already linked to a discord account.");
		if(manager.getAccountFromDiscordID(user.getId()) != null || manager.getPartialLinkFromUser(user) != null)
			return ImmutableList.of("'" + user.getName() + "' is already linked to a minecraft account.");
		if(manager.getPendingLinkFromUser(user) != null)
			return ImmutableList.of("'" + user.getName() + "' already has a pending link.");
		for(int i = 0; i < manager.partiallyLinkedAccounts.size(); i++)
		{
			PartialLinkedAccount partialLink = manager.partiallyLinkedAccounts.get(i);
			if(partialLink.discordID.equals(user.getId()))
			{
				return ImmutableList.of("'" + user.getName() + "' is already linked to a minecraft account.");
			}
			else if(partialLink.playerName.equals(playerName))
			{
				return ImmutableList.of("'" + playerName + "' is already linked to a discord account.");
			}
		}
		
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		List<ServerPlayer> players = server.getPlayerList().getPlayers();
		for(int i = 0; i < players.size(); i++)
		{
			if(players.get(i).getName().getString().equals(playerName))
			{
				LinkedAccount newAccount = new LinkedAccount(players.get(i).getUUID(), user.getId());
				manager.linkedAccounts.add(newAccount);
				manager.setDirty();
				LightmansConsole.LOGGER.info("Linked discord #" + user.getId() + " with '" + playerName + "'");
				return ImmutableList.of("Successfully linked " + user.getName() + " to '" + playerName +"'");
			}
		}
		PartialLinkedAccount partialLink = new PartialLinkedAccount(playerName, user.getId());
		manager.partiallyLinkedAccounts.add(partialLink);
		manager.setDirty();
		LightmansConsole.LOGGER.info("Partially linked discord #" + user.getId() + " with '" + playerName + "'");
		return ImmutableList.of("Successfully linked " + user.getName() + " to '" + playerName +"'");
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerLoggedInEvent event)
	{
		AccountManager manager = get();
		Player player = event.getPlayer();
		for(int i = 0; i < manager.partiallyLinkedAccounts.size(); i++)
		{
			if(manager.partiallyLinkedAccounts.get(i).playerName.equals(player.getName().getString()))
			{
				//Finalize the link
				LinkedAccount newAccount = new LinkedAccount(player.getUUID(), manager.partiallyLinkedAccounts.get(i).discordID);
				manager.linkedAccounts.add(newAccount);
				manager.partiallyLinkedAccounts.remove(i);
				manager.setDirty();
				LightmansConsole.LOGGER.info("Finalized linking discord #" + newAccount.discordID + " with '" + player.getName().getString() + "'");
				return;
			}
		}
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
