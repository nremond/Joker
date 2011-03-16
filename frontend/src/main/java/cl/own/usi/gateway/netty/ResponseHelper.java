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

/**
 * Write content to the channel and close the connection.
 * 
 * @author bperroud
 *
 */
public class ResponseHelper {

	private static final String HEADER_SERVER_LABEL = "Server";
	private static final String HEADER_SERVER_VALUE = "Joker1.0";
	private static final String HEADER_CONNECTION_LABEL = "Connection";
	private static final String HEADER_CONNECTION_VALUE = "close";

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
		response.setHeader(HEADER_SERVER_LABEL, HEADER_SERVER_VALUE);
		response.setHeader(HEADER_CONNECTION_LABEL, HEADER_CONNECTION_VALUE);
		response.setContent(buf);

		ChannelFuture future = e.getChannel().write(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}
		

}
