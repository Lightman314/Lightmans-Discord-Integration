package io.github.lightman314.lightmansdiscord.compat.vanish;

import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import io.github.lightman314.lightmansdiscord.compat.PlayerVisibilityUtil;
import io.github.lightman314.lightmansdiscord.discord.listeners.chat.ChatMessageListener;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.api.PlayerVanishEvent;

public class VanishModCompat {

	private static boolean loggedError = false;

	public static void init() {
		MinecraftForge.EVENT_BUS.register(VanishModCompat.class);
		PlayerVisibilityUtil.addPlayerHideFilter(VanishModCompat::isVanished);
	}
	
	public static boolean isVanished(Player player) {
		try {
			return VanishUtil.isVanished(player);
		} catch(Throwable t) {
			if(!loggedError)
			{
				loggedError = true;
				LightmansDiscordIntegration.LOGGER.error("Error found when check VanishMod for a players vanished status.\nVanishMod compatibility will not function!", t);
			}
			return false;
		}
	}
	
	@SubscribeEvent
	public static void onPlayerVanish(PlayerVanishEvent event) {
		if(event.isVanished())
			ChatMessageListener.fakePlayerLeave(event.getEntity(), false);
		else
			ChatMessageListener.fakePlayerJoin(event.getEntity());
	}
	
}
