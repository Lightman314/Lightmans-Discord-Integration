package io.github.lightman314.lightmansdiscord.util;

import java.util.Objects;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PlayerUtil {

	public static GameProfile playerProfile(UUID playerID)
	{
		return Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer().getProfileCache()).get(playerID).orElse(null);
	}
	
	public static String playerName(UUID playerID)
	{
		GameProfile profile = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer().getProfileCache()).get(playerID).orElse(null);
		return profile == null ? playerID.toString() : profile.getName();
	}

	public static String playerName(Entity player)
	{
		if(player == null)
			return "NULL";
		return MessageUtil.clearFormatting(player.getDisplayName().getString());
	}
	
}
