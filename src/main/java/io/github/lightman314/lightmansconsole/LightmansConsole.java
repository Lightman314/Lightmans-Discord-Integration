package io.github.lightman314.lightmansconsole;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.security.auth.login.LoginException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.lightman314.lightmansconsole.events.JDAInitializedEvent;
import io.github.lightman314.lightmansconsole.proxy.Proxy;
import io.github.lightman314.lightmansconsole.proxy.ServerProxy;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LightmansConsole.MODID)
public class LightmansConsole
{
	
	public static final String MODID = "lightmansconsole";
	
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Proxy PROXY = DistExecutor.safeRunForDist(() -> Proxy::new, () -> ServerProxy::new);
    
    private static boolean lightmansCurrencyLoaded = false;
    public static boolean isLightmansCurrencyLoaded() { return lightmansCurrencyLoaded; }

    public LightmansConsole() {
        // Register the setup methods
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
        
        //Register Config
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        lightmansCurrencyLoaded = ModList.get().isLoaded("lightmanscurrency");
        
        //LOGGER.info("Proxy is of type: " + PROXY.getClass().getName());
        
    }
    
    //Load the JDA after the config is loaded, to assure that we load the correct values
    public void onConfigLoad(ModConfig.Loading event) {
    	
    	if(event.getConfig().getModId().equals(MODID) && event.getConfig().getSpec() == Config.serverSpec)
    	{
    		try{
    			PROXY.initializeJDA();
        		MinecraftForge.EVENT_BUS.post(new JDAInitializedEvent(PROXY));
    		} catch(LoginException exception) {
    			LOGGER.error(exception.getMessage());
    		}
    	}
        
    }
    
}
