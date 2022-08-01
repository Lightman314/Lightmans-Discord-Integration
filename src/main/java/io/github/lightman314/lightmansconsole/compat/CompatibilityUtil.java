package io.github.lightman314.lightmansconsole.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

public class CompatibilityUtil {

	private static final List<PlayerListFilter> playerListFilters = new ArrayList<>();
	public static void addPlayerListFilter(PlayerListFilter filter) { playerListFilters.add(Objects.requireNonNull(filter)); }
	
	public static boolean isPlayerVisible(Player player) {
		if(LightmansDiscordIntegration.isVanishmodLoaded() && VanishModCompat.isVanished(player))
			return false;
		return true;
	}
	
	public static List<ServerPlayer> getPlayerList() {
		
		//Get the actual player list
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		List<ServerPlayer> players = server == null ? new ArrayList<>() : server.getPlayerList().getPlayers();
		//Filter out the hidden players
		return players.stream().filter(p -> {
			for(PlayerListFilter f : playerListFilters)
			{
				if(f.hidePlayer(p))
					return false;
			}
			return true;
		}).collect(Collectors.toList());
	}
	
	public static interface PlayerListFilter { boolean hidePlayer(ServerPlayer player); }
	
}