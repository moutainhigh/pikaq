package io.github.pikaq.server;


import io.github.pikaq.InvokeCallback;
import io.github.pikaq.RemotingContext;
import io.github.pikaq.common.exception.RemotingSendRequestException;
import io.github.pikaq.common.exception.RemotingTimeoutException;
import io.github.pikaq.protocol.command.RemotingCommand;

public class DefaultServer extends AbstractServer {

	private String serverName;
	

	public DefaultServer(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	protected void doStart(RemotingContext context) {
		
	}
	
	@Override
	protected void doClose() {

	}

	@Override
	public RemotingCommand invokeSync(RemotingCommand request, long timeoutMillis)
			throws RemotingTimeoutException, RemotingSendRequestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void invokeAsync(RemotingCommand request, InvokeCallback invokeCallback)
			throws RemotingSendRequestException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void invokeOneway(RemotingCommand request) throws RemotingSendRequestException {
		// TODO Auto-generated method stub
		
	}

}