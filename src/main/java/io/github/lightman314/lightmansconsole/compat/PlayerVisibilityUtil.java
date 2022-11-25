package io.github.lightman314.lightmansconsole.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PlayerVisibilityUtil {

	private static final List<Function<Player,Boolean>> playerListFilters = new ArrayList<>();

	/**
	 * Add filter to determine whether a player is 'hidden' or not.
	 * Should return true if the player is hidden, and false if they are visible.
	 */
	public static void addPlayerHideFilter(Function<Player,Boolean> filter) { playerListFilters.add(Objects.requireNonNull(filter)); }

	public static boolean isPlayerVisible(Player player) {
		for(Function<Player,Boolean> f : playerListFilters) {
			if(f.apply(player))
				return false;
		}
		return true;
	}

	public static List<ServerPlayer> getPlayerList() {
		//Get the actual player list
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		List<ServerPlayer> players = server == null ? new ArrayList<>() : server.getPlayerList().getPlayers();
		//Filter out the hidden players
		return players.stream().filter(PlayerVisibilityUtil::isPlayerVisible).collect(Collectors.toList());
	}

}