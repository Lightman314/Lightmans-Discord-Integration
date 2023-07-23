package io.github.lightman314.lightmansdiscord.util;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.server.ServerLifecycleHooks;

public class PlayerUtil {

	public static GameProfile playerProfile(UUID playerID)
	{
		return ServerLifecycleHooks.getCurrentServer().getProfileCache().get(playerID).orElse(null);
	}
	
	public static String playerName(UUID playerID)
	{
		GameProfile profile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(playerID).orElse(null);
		return profile == null ? playerID.toString() : profile.getName();
	}
	
}
