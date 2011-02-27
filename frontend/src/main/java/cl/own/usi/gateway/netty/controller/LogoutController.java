package cl.own.usi.gateway.netty.controller;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.springframework.stereotype.Component;

/**
 * Controller that logout the {@link User}
 * 
 * @author bperroud
 * @author nicolas
 */
@Component
public class LogoutController extends AbstractController {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

	}

}
