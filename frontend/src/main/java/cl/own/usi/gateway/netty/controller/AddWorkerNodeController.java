package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.gateway.client.WorkerClient;

/**
 * Controller to subscribe new worker to the client pool.
 * 
 * @author bperroud
 *
 */
@Component
public class AddWorkerNodeController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private WorkerClient workerClient;
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
		
		String[] splits = request.getUri().split("/");
		if (splits.length == 5) {
			String host = splits[3];
			int port = Integer.parseInt(splits[4]);
			boolean added = workerClient.addWorkerNode(host, port);
			if (added) {
				logger.debug("New worker join the cluster {}:{}", host, port);
			} else {
				logger.debug("Worker still alive {}:{}", host, port);
			}
			writeResponse(e, CREATED);
		} else {
			writeResponse(e, NOT_IMPLEMENTED);
		}
		
	}

}
