package io.github.lightman314.lightmansconsole.proxy;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;

public class Proxy {

	public void initializeJDA() throws LoginException{}
	
	public JDA getJDA() { return null; }
	
	public void clearJDA() {}
	
	public void addListener(Object listener) {}
	
}
