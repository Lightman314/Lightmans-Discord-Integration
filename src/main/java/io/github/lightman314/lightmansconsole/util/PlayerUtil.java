package io.github.lightman314.lightmansconsole.util;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PlayerUtil {

	public static GameProfile playerProfile(UUID playerID)
	{
		return ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getProfileByUUID(playerID);
	}
	
	public static String playerName(UUID playerID)
	{
		GameProfile profile = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getProfileByUUID(playerID);
		return profile == null ? playerID.toString() : profile.getName();
	}
	
}
