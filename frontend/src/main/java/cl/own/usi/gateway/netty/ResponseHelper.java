package cl.own.usi.gateway.netty;

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

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

	public static void writeResponse(final MessageEvent e,
			final HttpResponseStatus status) {
		final HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
		final ChannelFuture future = e.getChannel().write(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	public static void writeStringToReponse(final String s, final MessageEvent e) {
		writeStringToReponse(s, e, HttpResponseStatus.OK);
	}

	public static void writeStringToReponse(final String s,
			final MessageEvent e, final HttpResponse response) {

		writeBytesToResponse(s.getBytes(CharsetUtil.UTF_8), e, response);

	}
	
	public static void writeStringToReponse(final String s,
			final MessageEvent e, final HttpResponseStatus status) {

		final HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
		
		writeStringToReponse(s, e, response);
		
	}
	
	public static void writeBytesToResponse(final byte[] b, final MessageEvent e, final HttpResponse response) {
		
		final ChannelBuffer buf = ChannelBuffers.wrappedBuffer(b);

		response.setHeader(HEADER_SERVER_LABEL, HEADER_SERVER_VALUE);
		response.setHeader(HEADER_CONNECTION_LABEL, HEADER_CONNECTION_VALUE);
		response.setContent(buf);

		final ChannelFuture future = e.getChannel().write(response);
		future.addListener(ChannelFutureListener.CLOSE);
		
	}

}
