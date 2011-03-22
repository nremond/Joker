package cl.own.usi.gateway.netty.controller;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

import java.util.Set;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Controller.
 *
 * @author bperroud
 * @author nicolas
 *
 */
public abstract class AbstractController {

	protected static final String COOKIE_AUTH_NAME = "session_key";
	public static final String URI_API = "/api";
	public static final int URI_API_LENGTH = URI_API.length();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	abstract public void messageReceived(ChannelHandlerContext ctx,
			MessageEvent e) throws Exception;

	protected Logger getLogger() {
		return logger;
	}

	protected String getCookie(HttpRequest request, String name) {
		String cookieString = request.getHeader(COOKIE);

		if (cookieString != null) {
			Set<Cookie> cookies = new CookieDecoder().decode(cookieString);

			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

}
