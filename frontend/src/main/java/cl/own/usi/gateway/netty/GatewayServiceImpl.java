package cl.own.usi.gateway.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Netty initialization stuff.
 * 
 * @author bperroud
 * 
 */
@Component
public class GatewayServiceImpl implements InitializingBean, DisposableBean {

	private int port;

	@Autowired
	private ChannelPipelineFactory channelPipelineFactory;

	private Channel serverChannel;
	private ServerBootstrap bootstrap;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GatewayServiceImpl.class);

	@Value(value = "${frontend.nettyPort:9080}")
	public void setPort(int port) {
		this.port = port;
	}

	private boolean fixedWorkerThreadPool = false;

	@Value(value = "${frontend.netty.fixedWorkerThreadPool:false}")
	public void setFixedWorkerThreadPool(boolean fixedWorkerThreadPool) {
		this.fixedWorkerThreadPool = fixedWorkerThreadPool;
	}

	private int numberOfWorkerThreads;

	@Value(value = "${frontend.netty.numberOfWorkerThreads:4}")
	public void setNumberOfWorkerThreads(int numberOfWorkerThreads) {
		this.numberOfWorkerThreads = numberOfWorkerThreads;
	}

	public void setChannelPipelineFactory(
			ChannelPipelineFactory channelPipelineFactory) {
		this.channelPipelineFactory = channelPipelineFactory;
	}

	public void afterPropertiesSet() throws Exception {

		LOGGER.info("Netty configured for listening on port {}", port);

		Executor workerExecutor;
		if (fixedWorkerThreadPool) {
			workerExecutor = Executors
					.newFixedThreadPool(numberOfWorkerThreads);
		} else {
			workerExecutor = Executors.newCachedThreadPool();
		}

		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), workerExecutor,
				numberOfWorkerThreads));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(channelPipelineFactory);

		// Bind and start to accept incoming connections.
		serverChannel = bootstrap.bind(new InetSocketAddress(port));

	}

	public void destroy() throws Exception {

		serverChannel.unbind();

		bootstrap.releaseExternalResources();

	}
}
