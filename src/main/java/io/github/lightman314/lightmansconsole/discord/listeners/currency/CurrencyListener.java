package io.github.lightman314.lightmansconsole.discord.listeners.currency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmansconsole.Config;
import io.github.lightman314.lightmansconsole.LightmansConsole;
import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.listeners.types.SingleChannelListener;
import io.github.lightman314.lightmansconsole.util.MessageUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.UniversalTraderEvent.UniversalTradeCreateEvent;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData.ItemTradeType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public class CurrencyListener extends SingleChannelListener{
	
	private final Timer timer;
	
	private static final long PENDING_MESSAGE_TIMER = 300000; //5m timer cycle for sending pending messages.
	private static final long ANNOUCEMENT_DELAY = 60000; //60s delay before announcing to give the owner time to set a name, etc.
	
	Map<String,List<String>> pendingMessages = new HashMap<>();
	
	public CurrencyListener(Supplier<String> consoleChannel)
	{
		super(consoleChannel, () -> LightmansConsole.PROXY.getJDA());
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(new NotifyTraderOwnerTask(this), 0, PENDING_MESSAGE_TIMER);
	}

	@Override
	protected void onChannelMessageReceived(MessageReceivedEvent event) {
		
		handleMessage(event.getChannel(), event.getMessage(), event.getAuthor());
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event)
	{
		handleMessage(event.getChannel(), event.getMessage(), event.getAuthor());
	}
	
	private void handleMessage(MessageChannel channel, Message message, User author)
	{
		if(author.isBot())
			return;
		
		//Run command
		String input = message.getContentDisplay();
		String prefix = Config.SERVER.currencyCommandPrefix.get();
		if(input.startsWith(prefix))
		{
			String command = input.substring(prefix.length());
			if(command.startsWith("help"))
			{
				List<String> output = new ArrayList<>();
				output.add(prefix + "notifications <help|enable|disable> - Handle private currency notifications.");
				output.add(prefix + "search <sales|purchases|barters|all> [searchText] - List all universal trades selling items containing the searchText. Leave searchText empty to see all sales/purchases/barters.");
				output.add(prefix + "search <players|shops> [searchText] - List all trades for universal traders with player/shop names containing the searchText. Leave searchText empty to see all traders trades.");
				MessageUtil.sendTextMessage(channel, output);
			}
			else if(command.startsWith("notifications "))
			{
				String subcommand = command.substring(14);
				if(subcommand.startsWith("help"))
				{
					List<String> output = new ArrayList<>();
					if(AccountManager.currencyNotificationsEnabled(author))
						output.add("Personal notifications are enabled.");
					else
						output.add("Personal notifications are disabled.");
					output.add("If personal notifications are enabled you will receive notifications for the following:");
					output.add("-Purchases made on traders you own.");
					output.add("-When your trader is out of stock.");
					
					MessageUtil.sendTextMessage(channel, output);
				}
				else if(subcommand.startsWith("enable"))
				{
					if(AccountManager.enableCurrencyNotifications(author))
						MessageUtil.sendTextMessage(channel, "Personal notifications are now enabled.");
					else
						MessageUtil.sendTextMessage(channel, "Personal notifications were already enabled.");
				}
				else if(subcommand.startsWith("disable"))
				{
					if(AccountManager.disableCurrencyNotifications(author))
						MessageUtil.sendTextMessage(channel, "Personal notifications are now disabled.");
					else
						MessageUtil.sendTextMessage(channel, "Personal notifications were already disabled.");
				}
			}
			else if(command.startsWith("search "))
			{
				String subcommand = command.substring(7);
				AtomicReference<String> searchText = new AtomicReference<>("");
				AtomicBoolean findSales = new AtomicBoolean(false);
				AtomicBoolean findPurchases = new AtomicBoolean(false);
				AtomicBoolean findBarters = new AtomicBoolean(false);
				AtomicBoolean findOwners = new AtomicBoolean(false);
				AtomicBoolean findTraders = new AtomicBoolean(false);
				if(subcommand.startsWith("sales"))
				{
					findSales.set(true);
					if(subcommand.length() > 6)
						searchText.set(subcommand.substring(6).toLowerCase());
				}
				else if(subcommand.startsWith("purchases"))
				{
					findPurchases.set(true);
					if(subcommand.length() > 10)
						searchText.set(subcommand.substring(10).toLowerCase());
				}
				else if(subcommand.startsWith("barters"))
				{
					findBarters.set(true);
					if(subcommand.length() > 8)
						searchText.set(subcommand.substring(8).toLowerCase());
				}
				else if(subcommand.startsWith("players"))
				{
					findOwners.set(true);
					if(subcommand.length() > 8)
						searchText.set(subcommand.substring(8).toLowerCase());
				}
				else if(subcommand.startsWith("shops"))
				{
					findTraders.set(true);
					if(subcommand.length() > 6)
						searchText.set(subcommand.substring(6).toLowerCase());
				}
				else if(subcommand.startsWith("all"))
				{
					//All
					findSales.set(true);
					findPurchases.set(true);
					findBarters.set(true);
					//findOwners.set(true);
					//findTraders.set(true);
					if(subcommand.length() > 4)
						searchText.set(subcommand.substring(4).toLowerCase());
				}
				List<String> output = new ArrayList<>();
				TradingOffice.getTraders().forEach(trader -> {
					if(trader instanceof UniversalItemTraderData) //Can't search for non item traders at this time
					{
						UniversalItemTraderData itemTrader = (UniversalItemTraderData)trader;
						boolean listTrader = (findOwners.get() && (searchText.get().isEmpty() || itemTrader.getOwnerName().toLowerCase().contains(searchText.get())))
								|| (findTraders.get() && (searchText.get().isEmpty() || itemTrader.getName().getString().toLowerCase().contains(searchText.get())));
						if(listTrader)
						{
							AtomicBoolean firstTrade = new AtomicBoolean(true);
							itemTrader.getAllTrades().forEach(trade ->{
								if(trade.isValid())
								{
									if(firstTrade.get())
									{
										output.add("--" + itemTrader.getOwnerName() + "'s **" + itemTrader.getName().getString() + "**--");
										firstTrade.set(false);
									}
									if(trade.isSale())
									{
										ItemStack sellItem = trade.getSellItem();
										String itemName = getItemName(sellItem, trade.getCustomName());
										String priceText = trade.getCost().getString();
										output.add("Selling " + sellItem.getCount() + "x " + itemName + " for " + priceText);
									}
									else if(trade.isPurchase())
									{
										ItemStack sellItem = trade.getSellItem();
										String itemName = getItemName(sellItem, "");
										String priceText = trade.getCost().getString();
										output.add("Purchasing " + sellItem.getCount() + "x " + itemName + " for " + priceText);
									}
									else if(trade.isBarter())
									{
										ItemStack sellItem = trade.getSellItem();
										String sellItemName = getItemName(sellItem, trade.getCustomName());
										ItemStack barterItem = trade.getBarterItem();
										String barterItemName = getItemName(barterItem, "");
										output.add("Bartering " + barterItem.getCount() + "x " + barterItemName + " for " + sellItem.getCount() + "x " + sellItemName);
									}
								}
							});
						}
						else
						{
							itemTrader.getAllTrades().forEach(trade ->{
								if(trade.isValid())
								{
									if(trade.isSale() && findSales.get())
									{
										ItemStack sellItem = trade.getSellItem();
										String itemName = getItemName(sellItem, trade.getCustomName());
										
										//LightmansConsole.LOGGER.info("Item Name: " + itemName.toString());
										if(searchText.get().isEmpty() || itemName.toString().toLowerCase().contains(searchText.get()))
										{
											//Passed the search
											String priceText = trade.getCost().getString();
											output.add(itemTrader.getOwnerName() + " is selling " + sellItem.getCount() + "x " + itemName + " at " + itemTrader.getName().getString() + " for " + priceText);
										}
									}
									else if(trade.isPurchase() && findPurchases.get())
									{
										ItemStack sellItem = trade.getSellItem();
										String itemName = getItemName(sellItem, "");
										
										//LightmansConsole.LOGGER.info("Item Name: " + itemName.toString());
										if(searchText.get().isEmpty() || itemName.toLowerCase().contains(searchText.get()))
										{
											//Passed the search
											String priceText = trade.getCost().getString();
											output.add(itemTrader.getOwnerName() + " is buying " + sellItem.getCount() + "x " + itemName + " at " + itemTrader.getName().getString() + " for " + priceText);
										}
									}
									else if(trade.isBarter() && findBarters.get())
									{
										ItemStack sellItem = trade.getSellItem();
										String sellItemName = getItemName(sellItem, trade.getCustomName());
										
										ItemStack barterItem = trade.getBarterItem();
										String barterItemName = getItemName(barterItem,"");
										
										if(searchText.get().isEmpty() || sellItemName.toLowerCase().contains(searchText.get()) || barterItemName.toLowerCase().contains(searchText.get()))
										{
											output.add(itemTrader.getOwnerName() + " is bartering " + barterItem.getCount() + "x " + barterItemName + " for " + sellItem.getCount() + "x " + sellItemName + " at " + itemTrader.getName().getString());
										}
										
									}
								}
							});
						}
					}
				});
				if(output.size() > 0)
					MessageUtil.sendTextMessage(channel, output);
				else
					MessageUtil.sendTextMessage(channel, "No results found.");
				
			}
		}
	}
	
	private static String getItemName(ItemStack item, String customName)
	{
		//Ignore custom names on purchases
		StringBuffer itemName = new StringBuffer();
		if(customName.isEmpty())
			itemName.append(item.getDisplayName().getString());
		else
			itemName.append("*").append(customName).append("*");
		//Get enchantment data (if present)
		AtomicBoolean firstEnchantment = new AtomicBoolean(true);
		EnchantmentHelper.getEnchantments(item).forEach((enchantment, level) ->{
			if(firstEnchantment.get())
			{
				itemName.append(" [").append(enchantment.getDisplayName(level).getString());
				firstEnchantment.set(false);
			}
			else
				itemName.append(", ").append(enchantment.getDisplayName(level).getString());
		});
		if(!firstEnchantment.get()) //If an enchantment was gotten, append the end
			itemName.append("]");
		
		return itemName.toString();
	}
	
	@SubscribeEvent
	public void onTradeCarriedOut(PostTradeEvent event)
	{
		LinkedAccount account = AccountManager.getLinkedAccountFromPlayerID(event.getTrader().getOwnerID());
		if(account != null)
		{
			User linkedUser = this.getJDA().getUserById(account.discordID);
			if(AccountManager.currencyNotificationsEnabled(linkedUser))
			{
				if(event.getTrade() instanceof ItemTradeData)
				{
					ItemTradeData itemTrade = (ItemTradeData)event.getTrade();
					StringBuffer message = new StringBuffer();
					//Customer name
					message.append(event.getPlayer().getName().getString());
					//Action (bought, sold, ???)
					boolean isBarter = itemTrade.getTradeType() == ItemTradeType.BARTER;
					switch(itemTrade.getTradeType())
					{
					case SALE: message.append(" bought "); break;
					case PURCHASE: message.append(" sold "); break;
					case BARTER: message.append( "bartered "); break;
						default: message.append(" ??? ");
					}
					if(isBarter)
					{
						//Item given
						ItemStack barteredItem = itemTrade.getBarterItem();
						message.append(barteredItem.getCount()).append(" ").append(barteredItem.getDisplayName().getString());
						//Item bought
						ItemStack boughtItem = itemTrade.getSellItem();
						String boughtItemName = itemTrade.getCustomName().isEmpty() ? boughtItem.getDisplayName().getString() : itemTrade.getCustomName();
						message.append(boughtItem.getCount()).append(" ").append(boughtItemName);
					}
					else
					{
						//Item bought/sold
						ItemStack boughtItem = itemTrade.getSellItem();
						String boughtItemName = itemTrade.getCustomName().isEmpty() || itemTrade.getTradeType() != ItemTradeType.SALE ? boughtItem.getDisplayName().getString() : itemTrade.getCustomName();
						message.append(boughtItem.getCount()).append(" ").append(boughtItemName);
						//Price
						message.append(" for ");
						message.append(event.getPricePaid().getString());
					}
					//From trader name
					message.append(" from your ").append(event.getTrader().getName().getString());
					
					//Send the message directly to the linked user
					//Create as pending message to avoid message spamming them when a player buys a ton of the same item
					this.addPendingMessage(linkedUser, message.toString());
					//MessageUtil.sendPrivateMessage(linkedUser, message.toString());
					
					//Check if out of stock
					if(event.getTrader() instanceof IItemTrader)
					{
						if(itemTrade.stockCount((IItemTrader)event.getTrader()) < 1)
						{
							this.addPendingMessage(linkedUser, "**This trade is now out of stock!**");
							//MessageUtil.sendPrivateMessage(linkedUser, "**This trade is now out of stock!**");
						}
					}
				}
			}
		}
	}
	
	public void addPendingMessage(User user, String message)
	{
		String userId = user.getId();
		List<String> pendingMessages = this.pendingMessages.containsKey(userId) ? this.pendingMessages.get(userId) : Lists.newArrayList();
		pendingMessages.add(message);
		this.pendingMessages.put(userId, pendingMessages);
	}
	
	public void addPendingMessage(User user, List<String> messages)
	{
		String userId = user.getId();
		List<String> pendingMessages = this.pendingMessages.containsKey(userId) ? this.pendingMessages.get(userId) : Lists.newArrayList();
		pendingMessages.addAll(messages);
		this.pendingMessages.put(userId, pendingMessages);
	}
	
	public void sendPendingMessages()
	{
		//LightmansConsole.LOGGER.info("Sending Pending Messages");
		this.pendingMessages.forEach((userId, messages)->{
			User user = this.getJDA().getUserById(userId);
			if(user != null)
				MessageUtil.sendPrivateMessage(user, messages);
		});
		this.pendingMessages.clear();
	}
	
	@SubscribeEvent
	public void onUniversalTraderRegistered(UniversalTradeCreateEvent event)
	{
		//Announce the creation of the trader 60s later
		new Timer().schedule(new AnnouncementTask(this, event), ANNOUCEMENT_DELAY);
	}
	
	@SubscribeEvent
	public void onServerStop(FMLServerStoppingEvent event)
	{
		//Cancel the timer
		this.timer.cancel();
		this.sendPendingMessages();
	}
	
	private static class NotifyTraderOwnerTask extends TimerTask
	{
		private final CurrencyListener cl;
		public NotifyTraderOwnerTask(CurrencyListener cl) { this.cl = cl; }
		@Override
		public void run() { this.cl.sendPendingMessages(); }
	}

	private static class AnnouncementTask extends TimerTask
	{
		
		private final CurrencyListener cl;
		private final UniversalTradeCreateEvent event;

		public AnnouncementTask(CurrencyListener cl, UniversalTradeCreateEvent event) {
			this.cl = cl;
			this.event = event;
		}
		
		@Override
		public void run() {
			if(this.event.getData() == null) //Abort if the trader was removed.
				return;
			cl.sendTextMessage(this.event.getOwner().getName().getString() + " has made a new Universal Trader" + (event.getData().hasCustomName() ? " '" + event.getData().getName().getString() + "'!" : "!"));
		}
		
	}
	
}
