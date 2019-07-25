package io.github.pikaq.remoting.server;

import io.github.pikaq.remoting.Remoteable;

public interface Server extends Remoteable {
	
	void start();
	
	void shutdown();

	void registerShutdownHooks(Thread... hooks);

	String getServerName();
	
	void setServerConfig(ServerConfig serverConfig);
	
	ServerConfig getServerConfig();
}
