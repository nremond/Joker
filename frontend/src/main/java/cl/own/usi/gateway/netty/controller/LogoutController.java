package cl.own.usi.gateway.netty.controller;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.springframework.stereotype.Component;

@Component
public class LogoutController extends AbstractController {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

	}

}
