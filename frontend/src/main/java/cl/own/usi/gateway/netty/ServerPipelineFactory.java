package cl.own.usi.gateway.netty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.jboss.netty.channel.Channels.*;

/**
 * Netty pipeline.
 * 
 * Upstream pipeline decodes the message, send it to an execution worker and handle it. 
 * Downstream pipeline reencode the message.
 * 
 * @author bperroud
 *
 */
@Component
public class ServerPipelineFactory implements ChannelPipelineFactory, InitializingBean {

	private ExecutorService executor;
	
	@Autowired
	private ChannelUpstreamHandler upstreamHandler;
	
	private ExecutionHandler executionHandler;
	
	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public ChannelUpstreamHandler getUpstreamHandler() {
		return upstreamHandler;
	}

	public void setUpstreamHandler(ChannelUpstreamHandler upstreamHandler) {
		this.upstreamHandler = upstreamHandler;
	}

	public ChannelPipeline getPipeline() throws Exception {

		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("decoder", new HttpRequestDecoder());
		// Uncomment the following line if you don't want to handle HttpChunks.
		pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		// Remove the following line if you don't want automatic content
		// compression.
//		pipeline.addLast("deflater", new HttpContentCompressor());

		pipeline.addLast("executor1", executionHandler);
		pipeline.addLast("handler1", getUpstreamHandler());

		return pipeline;
	}

	public void afterPropertiesSet() throws Exception {
		if (getExecutor() == null) {
			setExecutor(Executors.newCachedThreadPool());
		}
		if (getUpstreamHandler() == null) {
			throw new NullPointerException("UpstreamHandler");
		}
		
		executionHandler = new ExecutionHandler(getExecutor());
	}
}
