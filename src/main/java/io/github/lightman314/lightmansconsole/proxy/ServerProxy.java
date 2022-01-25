package io.github.lightman314.lightmansconsole.proxy;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import io.github.lightman314.lightmansconsole.Config;
import io.github.lightman314.lightmansconsole.LightmansDiscordIntegration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class ServerProxy extends Proxy{
	
	private static JDA jda;
	
	private static List<Object> pendingListeners = new ArrayList<>();
	
	static boolean initialized = false;
	
	@Override
    public void initializeJDA() throws LoginException
    {
		
    	if(initialized)
    	{
    		LightmansDiscordIntegration.LOGGER.warn("Attempting to initialize the JDA twice.");
    		return;
    	}
    	initialized = true;
    	
    	String token = Config.SERVER.botToken.get();
    	
    	//Build the JDA
    	LightmansDiscordIntegration.LOGGER.info("Attempting to build the JDA.");
    	JDABuilder builder = JDABuilder.createDefault(token);
    	builder.setAutoReconnect(true);
    	builder.setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_EMOJIS);
    	builder.setMemberCachePolicy(MemberCachePolicy.ALL);
    	builder.setChunkingFilter(ChunkingFilter.ALL);
    	jda = builder.build();
    	
    	//Add the pending listeners
    	pendingListeners.forEach(listener -> this.addListener(listener));
    	pendingListeners.clear();
    	
    	//Wait until the jda is ready
    	LightmansDiscordIntegration.LOGGER.info("Waiting for the JDA to finish the log in process.");
    	try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	LightmansDiscordIntegration.LOGGER.info("JDA has successfully logged in.");
    }
	
	@Override
	public JDA getJDA()
	{
		return jda;
	}
	
	@Override
	public void clearJDA()
	{
		jda = null;
	}
	
	@Override
	public void addListener(Object listener)
	{
		if(jda == null)
			pendingListeners.add(listener);
		else
		{
			jda.addEventListener(listener); 
			LightmansDiscordIntegration.LOGGER.info("Added JDA listener of type " + listener.getClass().getName());
		}
	}
	
}
