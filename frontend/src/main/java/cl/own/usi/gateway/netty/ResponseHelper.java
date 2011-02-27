package cl.own.usi.gateway.netty;

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_0;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

public class ResponseHelper {

	private ResponseHelper() {
		// Utility class => hide default constructor
	}

	public static void writeResponse(MessageEvent e, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_0, status);
		ChannelFuture future = e.getChannel().write(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	public static void writeStringToReponse(String s, MessageEvent e,
			HttpResponseStatus status) {

		ChannelBuffer buf = ChannelBuffers.copiedBuffer(s, CharsetUtil.UTF_8);

		HttpResponse response = new DefaultHttpResponse(HTTP_1_0, status);
		response.setContent(buf);

		ChannelFuture future = e.getChannel().write(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

}
