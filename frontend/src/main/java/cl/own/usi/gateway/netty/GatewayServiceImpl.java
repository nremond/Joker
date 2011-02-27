package cl.own.usi.gateway.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Netty initialization stuff.
 * 
 * @author bperroud
 *
 */
@Component
public class GatewayServiceImpl implements InitializingBean, DisposableBean {

	private int port = 9080;
	
	@Autowired
	private ChannelPipelineFactory channelPipelineFactory;

	private Channel serverChannel;
	private ServerBootstrap bootstrap;
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ChannelPipelineFactory getChannelPipelineFactory() {
		return channelPipelineFactory;
	}

	public void setChannelPipelineFactory(
			ChannelPipelineFactory channelPipelineFactory) {
		this.channelPipelineFactory = channelPipelineFactory;
	}

	public void afterPropertiesSet() throws Exception {
		
		if (getChannelPipelineFactory() == null) {
			throw new NullPointerException("pipelineFactory");
		}
		
		bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(getChannelPipelineFactory());

		// Bind and start to accept incoming connections.
		serverChannel = bootstrap.bind(new InetSocketAddress(getPort()));
		
	}

	public void destroy() throws Exception {
		
		serverChannel.unbind();
		
		bootstrap.releaseExternalResources();
		
	}
}
