package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import cl.own.usi.cache.CacheManager;

/**
 * Abstract Controller.
 *
 * @author bperroud
 * @author nicolas
 *
 */
public abstract class AbstractController {

	@Autowired
	private CacheManager cacheManager;

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

	protected void writeHtml(final MessageEvent e, final Resource htmlTemplate,
			final Map<String, String> mapping) throws IOException {
		BufferedReader in = null;
		StringBuffer buff = new StringBuffer();
		try {
			final InputStream inputStream = htmlTemplate.getInputStream();

			in = new BufferedReader(new InputStreamReader(inputStream));
			String str;
			while ((str = in.readLine()) != null) {
				buff.append(str);
				buff.append("\n");
			}

		} catch (IOException exp) {
			getLogger().error("The html template couldn't be found at {}",
					htmlTemplate);
		} finally {
			if (in != null) {
				in.close();
			}
		}

		String html = buff.toString();
		for (Map.Entry<String, String> m : mapping.entrySet()) {
			html = html.replaceAll(m.getKey(), m.getValue());
		}

		writeStringToReponse(html, e, CREATED);
	}

	protected void writeHtml(MessageEvent e, Resource htmlTemplate)
			throws IOException {
		final Map<String, String> mapping = Collections.emptyMap();
		writeHtml(e, htmlTemplate, mapping);
	}

	protected CacheManager getCacheManager() {
		return cacheManager;
	}
}
