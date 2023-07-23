package io.github.lightman314.lightmansdiscord;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.lightman314.lightmansdiscord.compat.vanish.VanishModCompat;
import io.github.lightman314.lightmansdiscord.events.JDAInitializedEvent;
import io.github.lightman314.lightmansdiscord.message.MessageManager;
import io.github.lightman314.lightmansdiscord.proxy.Proxy;
import io.github.lightman314.lightmansdiscord.proxy.ServerProxy;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LightmansDiscordIntegration.MODID)
public class LightmansDiscordIntegration
{
	
	public static final String MODID = "lightmansdiscord";
	
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Proxy PROXY = DistExecutor.safeRunForDist(() -> Proxy::new, () -> ServerProxy::new);

    public LightmansDiscordIntegration() {
        // Register the setup methods
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
        
        //Flag it to ignore server-only setups
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a,b) -> true));
        
        //Register Config
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, LDIConfig.serverSpec);
    	
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(MessageManager::registerIncludedMessages);
        
        //Check if the vanish mod is installed
        if(ModList.get().isLoaded("vmod"))
        {
        	VanishModCompat.init();
        	LOGGER.info("Vanish mod detected. Vanish mod compatibility enabled.");
        }
       
    }
    
    private void serverSetup(FMLDedicatedServerSetupEvent event)
    {
    	MessageManager.collectEntries();
    }
    
    //Load the JDA after the config is loaded, to assure that we load the correct values
    private void onConfigLoad(ModConfigEvent.Loading event) {
    	
    	if(event.getConfig().getModId().equals(MODID) && event.getConfig().getSpec() == LDIConfig.serverSpec)
    	{
    		try{
    			PROXY.initializeJDA();
    			try {
    				MinecraftForge.EVENT_BUS.post(new JDAInitializedEvent(PROXY));
    			} catch(Throwable t) { LOGGER.error("Error initializing JDA listeners.", t); }
    		} catch(Throwable t) {
    			LOGGER.error("Error setting up the JDA.", t);
    		}
    	}
        
    }
    
}
