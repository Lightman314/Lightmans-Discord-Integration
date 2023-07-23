package io.github.lightman314.lightmansdiscord.proxy;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmansdiscord.LDIConfig;
import io.github.lightman314.lightmansdiscord.LightmansDiscordIntegration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class ServerProxy extends Proxy{
	
	private static JDA jda;
	
	private static final List<Object> pendingListeners = new ArrayList<>();
	
	static boolean initialized = false;
	
	@Override
    public void initializeJDA()
    {
		
    	if(initialized)
    	{
    		LightmansDiscordIntegration.LOGGER.warn("Attempting to initialize the JDA twice.");
    		return;
    	}
    	initialized = true;
    	
    	String token = LDIConfig.SERVER.botToken.get();
    	
    	//Build the JDA
    	LightmansDiscordIntegration.LOGGER.info("Attempting to build the JDA.");
    	JDABuilder builder = JDABuilder.createDefault(token);
    	builder.setAutoReconnect(true);
    	builder.setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.SCHEDULED_EVENTS);
    	builder.setMemberCachePolicy(MemberCachePolicy.ALL);
    	builder.setChunkingFilter(ChunkingFilter.ALL);
    	jda = builder.build();
    	
    	//Add the pending listeners
    	pendingListeners.forEach(this::addListener);
    	pendingListeners.clear();
    	
    	//Wait until the jda is ready
    	LightmansDiscordIntegration.LOGGER.info("Waiting for the JDA to finish the log in process.");
    	try {
			jda.awaitReady();
			LightmansDiscordIntegration.LOGGER.info("JDA has successfully logged in.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

    }
	
	@Override
	public JDA getJDA() { return jda; }
	
	@Override
	public void clearJDA() { jda = null; }
	
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
