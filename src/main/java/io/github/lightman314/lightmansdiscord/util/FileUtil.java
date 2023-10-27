package io.github.lightman314.lightmansdiscord.util;

import java.io.File;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.server.ServerLifecycleHooks;

public class FileUtil {

	static File worldFolder = null;
	
	public static File getWorldFolder()
	{
		if(worldFolder == null)
		{
			String worldName = "world";
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server instanceof DedicatedServer)
				worldName = ((DedicatedServer)server).getLevelIdName();
			worldFolder = new File(worldName);
		}
		return worldFolder;
	}
	
}
