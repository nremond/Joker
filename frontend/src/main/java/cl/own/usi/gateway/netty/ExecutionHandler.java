package cl.own.usi.gateway.netty;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.execution.ChannelEventRunnable;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.internal.ExecutorUtil;

public class ExecutionHandler implements ChannelUpstreamHandler,
		ExternalResourceReleasable {

	private final ExecutorService executor;

	public ExecutionHandler(ExecutorService executor) {
		if (executor == null) {
			throw new NullPointerException("executor");
		}
		this.executor = executor;
	}

	public Executor getExecutor() {
        return executor;
    }
	
	public void releaseExternalResources() {
        ExecutorUtil.terminate(getExecutor());
    }

	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		
		if (e instanceof MessageEvent) {
			executor.execute(new ChannelEventRunnable(ctx, e));
		}
		
	}

}
