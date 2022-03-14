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

import io.github.lightman314.lctech.common.universaldata.UniversalEnergyTraderData;
import io.github.lightman314.lctech.common.universaldata.UniversalFluidTraderData;
import io.github.lightman314.lctech.trader.energy.IEnergyTrader;
import io.github.lightman314.lctech.trader.fluid.IFluidTrader;
import io.github.lightman314.lctech.trader.tradedata.EnergyTradeData;
import io.github.lightman314.lctech.trader.tradedata.FluidTradeData;
import io.github.lightman314.lctech.util.EnergyUtil;
import io.github.lightman314.lctech.util.FluidFormatUtil;
import io.github.lightman314.lightmansconsole.Config;
import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import io.github.lightman314.lightmansconsole.discord.links.AccountManager;
import io.github.lightman314.lightmansconsole.discord.links.LinkedAccount;
import io.github.lightman314.lightmansconsole.discord.listeners.types.SingleChannelListener;
import io.github.lightman314.lightmansconsole.message.MessageManager;
import io.github.lightman314.lightmansconsole.util.MessageUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.UniversalTraderEvent.UniversalTradeCreateEvent;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData.ItemTradeType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;

public class CurrencyListener extends SingleChannelListener{
	
	private final Timer timer;
	
	private static final long PENDING_MESSAGE_TIMER = 300000; //5m timer cycle for sending pending messages.
	private static final long ANNOUCEMENT_DELAY = 60000; //60s delay before announcing to give the owner time to set a name, etc.
	
	Map<String,List<String>> pendingMessages = new HashMap<>();
	
	public CurrencyListener(Supplier<String> consoleChannel)
	{
		super(consoleChannel, () -> LightmansDiscordIntegration.PROXY.getJDA());
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
				output.add(prefix + "notifications <help|enable|disable> - " + MessageManager.M_HELP_LC_NOTIFICATIONS.get());
				output.add(prefix + "search <sales|purchases|barters|all> [searchText] - " + MessageManager.M_HELP_LC_SEARCH1.get());
				output.add(prefix + "search <players|shops> [searchText] - " + MessageManager.M_HELP_LC_SEARCH2.get());
				MessageUtil.sendTextMessage(channel, output);
			}
			else if(command.startsWith("notifications "))
			{
				String subcommand = command.substring(14);
				if(subcommand.startsWith("help"))
				{
					List<String> output = new ArrayList<>();
					output.add(AccountManager.currencyNotificationsEnabled(author) ? MessageManager.M_NOTIFICATIONS_ENABLED.get() : MessageManager.M_NOTIFICATIONS_DISABLED.get());
					output.addAll(Lists.newArrayList(MessageManager.M_NOTIFICATIONS_HELP.get().split("\n")));
					
					MessageUtil.sendTextMessage(channel, output);
				}
				else if(subcommand.startsWith("enable"))
				{
					if(AccountManager.enableCurrencyNotifications(author))
						MessageUtil.sendTextMessage(channel, MessageManager.M_NOTIFICATIONS_ENABLE_SUCCESS.get());
					else
						MessageUtil.sendTextMessage(channel, MessageManager.M_NOTIFICATIONS_ENABLE_FAIL.get());
				}
				else if(subcommand.startsWith("disable"))
				{
					if(AccountManager.disableCurrencyNotifications(author))
						MessageUtil.sendTextMessage(channel, MessageManager.M_NOTIFICATIONS_DISABLE_SUCCESS.get());
					else
						MessageUtil.sendTextMessage(channel, MessageManager.M_NOTIFICATIONS_DISABLE_FAIL.get());
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
					if(subcommand.length() > 10)
						searchText.set(subcommand.substring(10).toLowerCase());
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
					try {
						boolean listTrader = (findOwners.get() && (searchText.get().isEmpty() || trader.getCoreSettings().getOwnerName().toLowerCase().contains(searchText.get())))
								|| (findTraders.get() && (searchText.get().isEmpty() || trader.getName().getString().toLowerCase().contains(searchText.get())));
						
						if(trader instanceof UniversalItemTraderData) //Can't search for non item traders at this time
						{
							UniversalItemTraderData itemTrader = (UniversalItemTraderData)trader;
							if(listTrader)
							{
								boolean firstTrade = true;
								for(int i = 0; i < itemTrader.getTradeCount(); ++i)
								{
									ItemTradeData trade = itemTrader.getTrade(i);
									if(trade.isValid())
									{
										if(firstTrade)
										{
											output.add("--" + itemTrader.getCoreSettings().getOwnerName() + "'s **" + itemTrader.getName().getString() + "**--");
											firstTrade = false;
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
								}
							}
							else
							{
								for(int i = 0; i < itemTrader.getTradeCount(); ++i)
								{
									ItemTradeData trade = itemTrader.getTrade(i);
									if(trade.isValid())
									{
										if(trade.isSale() && findSales.get())
										{
											ItemStack sellItem = trade.getSellItem();
											String itemName = getItemName(sellItem, trade.getCustomName());
											
											//LightmansConsole.LOGGER.info("Item Name: " + itemName.toString());
											if(searchText.get().isEmpty() || itemName.toLowerCase().contains(searchText.get()))
											{
												//Passed the search
												String priceText = trade.getCost().getString();
												output.add(itemTrader.getCoreSettings().getOwnerName() + " is selling " + sellItem.getCount() + "x " + itemName + " at " + itemTrader.getName().getString() + " for " + priceText);
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
												output.add(itemTrader.getCoreSettings().getOwnerName() + " is buying " + sellItem.getCount() + "x " + itemName + " at " + itemTrader.getName().getString() + " for " + priceText);
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
												output.add(itemTrader.getCoreSettings().getOwnerName() + " is bartering " + barterItem.getCount() + "x " + barterItemName + " for " + sellItem.getCount() + "x " + sellItemName + " at " + itemTrader.getName().getString());
											}
											
										}
									}
								}
							}
						}
						else if(LightmansDiscordIntegration.isLCTechLoaded())
						{
							if(trader instanceof UniversalFluidTraderData)
							{
								UniversalFluidTraderData fluidTrader = (UniversalFluidTraderData)trader;
								if(listTrader)
								{
									boolean firstTrade = true;
									for(int i = 0; i < fluidTrader.getTradeCount(); ++i)
									{
										FluidTradeData trade = fluidTrader.getTrade(i);
										if(trade.isValid())
										{
											if(firstTrade)
											{
												output.add("--" + fluidTrader.getCoreSettings().getOwnerName() + "'s **" + fluidTrader.getName().getString() + "**--");
												firstTrade = false;
											}
											if(trade.isSale())
											{
												FluidStack sellFluid = trade.getProduct();
												String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();
												String priceText = trade.getCost().getString();
												output.add("Selling " + FluidFormatUtil.formatFluidAmount(trade.getQuantity()) + "mB of " + fluidName + " for " + priceText);
											}
											else if(trade.isPurchase())
											{
												FluidStack sellFluid = trade.getProduct();
												String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();
												String priceText = trade.getCost().getString();
												output.add("Purchasing " + FluidFormatUtil.formatFluidAmount(trade.getQuantity()) + "mB of " + fluidName + " for " + priceText);
											}
										}
									}
								}
								else
								{
									for(int i = 0; i < fluidTrader.getTradeCount(); ++i)
									{
										FluidTradeData trade = fluidTrader.getTrade(i);
										if(trade.isValid())
										{
											if(trade.isSale() && findSales.get())
											{
												FluidStack sellFluid = trade.getProduct();
												String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();
												
												//LightmansConsole.LOGGER.info("Item Name: " + itemName.toString());
												if(searchText.get().isEmpty() || fluidName.toLowerCase().contains(searchText.get()))
												{
													//Passed the search
													String priceText = trade.getCost().getString();
													output.add(fluidTrader.getCoreSettings().getOwnerName() + " is selling " + FluidFormatUtil.formatFluidAmount(trade.getQuantity()) + "mB of " + fluidName + " at " + fluidTrader.getName().getString() + " for " + priceText);
												}
											}
											else if(trade.isPurchase() && findPurchases.get())
											{
												FluidStack sellFluid = trade.getProduct();
												String fluidName = FluidFormatUtil.getFluidName(sellFluid).getString();
												
												//LightmansConsole.LOGGER.info("Item Name: " + itemName.toString());
												if(searchText.get().isEmpty() || fluidName.toLowerCase().contains(searchText.get()))
												{
													//Passed the search
													String priceText = trade.getCost().getString();
													output.add(fluidTrader.getCoreSettings().getOwnerName() + " is buying " + FluidFormatUtil.formatFluidAmount(trade.getQuantity()) + "x " + fluidName + " at " + fluidTrader.getName().getString() + " for " + priceText);
												}
											}
										}
									}
								}
								
							}
							else if(trader instanceof UniversalEnergyTraderData)
							{
								UniversalEnergyTraderData energyTrader = (UniversalEnergyTraderData)trader;
								if(listTrader)
								{
									boolean firstTrade = true;
									for(int i = 0; i < energyTrader.getTradeCount(); ++i)
									{
										EnergyTradeData trade = energyTrader.getTrade(i);
										if(trade.isValid())
										{
											if(firstTrade)
											{
												output.add("--" + energyTrader.getCoreSettings().getOwnerName() + "'s **" + energyTrader.getName().getString() + "**--");
												firstTrade = false;
											}
											if(trade.isSale())
											{
												String priceText = trade.getCost().getString();
												output.add("Selling " + EnergyUtil.formatEnergyAmount(trade.getAmount()) + " for " + priceText);
											}
											else if(trade.isPurchase())
											{
												String priceText = trade.getCost().getString();
												output.add("Purchasing " + EnergyUtil.formatEnergyAmount(trade.getAmount()) + " for " + priceText);
											}
										}
									}
								}
								else
								{
									for(int i = 0; i < energyTrader.getTradeCount(); ++i)
									{
										EnergyTradeData trade = energyTrader.getTrade(i);
										//Energy Trades always have the same product name ("FE" or "ENERGY") so perform the search check before knowing the trade type
										if(trade.isValid() && (searchText.get().isEmpty()|| EnergyUtil.ENERGY_UNIT.toLowerCase().contains(searchText.get()) || "Energy".toLowerCase().contains(searchText.get())))
										{
											if(trade.isSale() && findSales.get())
											{
												String priceText = trade.getCost().getString();
												output.add(energyTrader.getCoreSettings().getOwnerName() + " is selling " + EnergyUtil.formatEnergyAmount(trade.getAmount()) + " at " + energyTrader.getName().getString() + " for " + priceText);
											}
											else if(trade.isPurchase() && findPurchases.get())
											{
												String priceText = trade.getCost().getString();
												output.add(energyTrader.getCoreSettings().getOwnerName() + " is buying " + EnergyUtil.formatEnergyAmount(trade.getAmount()) + " at " + energyTrader.getName().getString() + " for " + priceText);
											}
										}
									}
								}
							}
						}
					} catch(Exception e) { e.printStackTrace(); }
				});
				if(output.size() > 0)
					MessageUtil.sendTextMessage(channel, output);
				else
					MessageUtil.sendTextMessage(channel, MessageManager.M_SEARCH_NORESULTS.get());
				
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
				itemName.append(" [").append(enchantment.getFullname(level).getString());
				firstEnchantment.set(false);
			}
			else
				itemName.append(", ").append(enchantment.getFullname(level).getString());
		});
		if(!firstEnchantment.get()) //If an enchantment was gotten, append the end
			itemName.append("]");
		
		return itemName.toString();
	}
	
	@SubscribeEvent
	public void onTradeCarriedOut(PostTradeEvent event)
	{
		try {
			PlayerReference recipient = event.getTrader().getCoreSettings().getOwner();
			if(event.getTrader().getCoreSettings().getTeam() != null)
			{
				recipient = event.getTrader().getCoreSettings().getTeam().getOwner();
			}
			LinkedAccount account = AccountManager.getLinkedAccountFromPlayerID(recipient.id);
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
						message.append(event.getPlayerReference().lastKnownName());
						//Action (bought, sold, ???)
						switch(itemTrade.getTradeType())
						{
						case SALE: message.append(" bought "); break;
						case PURCHASE: message.append(" sold "); break;
						case BARTER: message.append( "bartered "); break;
							default: message.append(" ??? ");
						}
						if(itemTrade.isBarter())
						{
							//Item given
							ItemStack barteredItem = itemTrade.getBarterItem();
							message.append(barteredItem.getCount()).append(" ").append(barteredItem.getHoverName().getString());
							//Item bought
							ItemStack boughtItem = itemTrade.getSellItem();
							String boughtItemName = itemTrade.getCustomName().isEmpty() ? boughtItem.getHoverName().getString() : itemTrade.getCustomName();
							message.append(boughtItem.getCount()).append(" ").append(boughtItemName);
						}
						else
						{
							//Item bought/sold
							ItemStack boughtItem = itemTrade.getSellItem();
							String boughtItemName = itemTrade.getCustomName().isEmpty() || itemTrade.getTradeType() != ItemTradeType.SALE ? boughtItem.getHoverName().getString() : itemTrade.getCustomName();
							message.append(boughtItem.getCount()).append(" ").append(boughtItemName);
							//Price
							message.append(" for ");
							if(event.getPricePaid().isFree() || event.getPricePaid().getRawValue() <= 0)
								message.append("free");
							else
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
								this.addPendingMessage(linkedUser, MessageManager.M_NOTIFICATION_OUTOFSTOCK.get());
								//MessageUtil.sendPrivateMessage(linkedUser, "**This trade is now out of stock!**");
							}
						}
					}
					else if(LightmansDiscordIntegration.isLCTechLoaded())
					{
						if(event.getTrade() instanceof FluidTradeData)
						{
							FluidTradeData fluidTrade = (FluidTradeData)event.getTrade();
							StringBuffer message = new StringBuffer();
							//Customer name
							message.append(event.getPlayerReference().lastKnownName());
							//Action (bought, sold, ???)
							switch(fluidTrade.getTradeDirection())
							{
							case SALE: message.append(" bought "); break;
							case PURCHASE: message.append(" sold "); break;
								default: message.append(" ??? ");
							}
							//Item bought/sold
							FluidStack boughtFluid = fluidTrade.getProduct();
							String boughtFluidName = FluidFormatUtil.getFluidName(boughtFluid).getString();
							message.append(FluidFormatUtil.formatFluidAmount(fluidTrade.getQuantity())).append("mB of ").append(boughtFluidName);
							//Price
							message.append(" for ");
							if(event.getPricePaid().isFree() || event.getPricePaid().getRawValue() <= 0)
								message.append("free");
							else
								message.append(event.getPricePaid().getString());
							
							//From trader name
							message.append(" from your ").append(event.getTrader().getName().getString());
							
							//Send the message directly to the linked user
							//Create as pending message to avoid message spamming them when a player buys a ton of the same item
							this.addPendingMessage(linkedUser, message.toString());
							//MessageUtil.sendPrivateMessage(linkedUser, message.toString());
							
							//Check if out of stock
							if(event.getTrader() instanceof IFluidTrader)
							{
								if(fluidTrade.getStock((IFluidTrader)event.getTrader()) < 1)
								{
									this.addPendingMessage(linkedUser, MessageManager.M_NOTIFICATION_OUTOFSTOCK.get());
								}
							}
						}
						else if(event.getTrade() instanceof EnergyTradeData)
						{
							EnergyTradeData energyTrade = (EnergyTradeData)event.getTrade();
							StringBuffer message = new StringBuffer();
							//Customer name
							message.append(event.getPlayerReference().lastKnownName());
							//Action (bought, sold, ???)
							switch(energyTrade.getTradeDirection())
							{
							case SALE: message.append(" bought "); break;
							case PURCHASE: message.append(" sold "); break;
								default: message.append(" ??? ");
							}
							//Item bought/sold
							message.append(EnergyUtil.formatEnergyAmount(energyTrade.getAmount()));
							//Price
							message.append(" for ");
							if(event.getPricePaid().isFree() || event.getPricePaid().getRawValue() <= 0)
								message.append("free");
							else
								message.append(event.getPricePaid().getString());
							
							//From trader name
							message.append(" from your ").append(event.getTrader().getName().getString());
							
							//Send the message directly to the linked user
							//Create as pending message to avoid message spamming them when a player buys a ton of the same item
							this.addPendingMessage(linkedUser, message.toString());
							//MessageUtil.sendPrivateMessage(linkedUser, message.toString());
							
							//Check if out of stock
							if(event.getTrader() instanceof IEnergyTrader)
							{
								if(energyTrade.getStock((IEnergyTrader)event.getTrader()) < 1)
								{
									this.addPendingMessage(linkedUser, MessageManager.M_NOTIFICATION_OUTOFSTOCK.get());
								}
							}
						}
					}
				}
			}
		} catch(Exception e) { e.printStackTrace(); }
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
		messages.forEach(message -> pendingMessages.add(message));
		this.pendingMessages.put(userId, pendingMessages);
	}
	
	public void sendPendingMessages()
	{
		//LightmansConsole.LOGGER.info("Sending Pending Messages");
		this.pendingMessages.forEach((userId, messages)->{
			try {
				User user = this.getJDA().getUserById(userId);
				if(user != null)
				{
					MessageUtil.sendPrivateMessage(user, messages);
				}	
			} catch(Exception e) { e.printStackTrace(); }
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
	public void onServerStop(ServerStoppingEvent event)
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
			try {
				if(this.event.getData() == null) //Abort if the trader was removed.
					return;
				if(event.getData().getCoreSettings().hasCustomName())
					cl.sendTextMessage(MessageManager.M_NEWTRADER_NAMED.format(this.event.getData().getCoreSettings().getOwnerName(), event.getData().getCoreSettings().getCustomName()));
				else
					cl.sendTextMessage(MessageManager.M_NEWTRADER.format(this.event.getData().getCoreSettings().getOwnerName()));
			} catch(Exception e) { e.printStackTrace(); }
		}
		
	}
	
}
