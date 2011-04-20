package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultCookie;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import cl.own.usi.cache.CachedUser;
import cl.own.usi.gateway.client.UserLogin;
import cl.own.usi.gateway.client.WorkerClient;
import cl.own.usi.json.LoginRequest;
import cl.own.usi.model.User;
import cl.own.usi.service.GameService;

/**
 * Controller that authenticate the {@link User}
 *
 * @author bperroud
 * @author nicolas
 */
@Component
public class LoginController extends AbstractController {

	@Autowired
	private GameService gameService;

	@Autowired
	private WorkerClient workerClient;

	@Value(value = "classpath:template/login.html")
	private Resource loginTemplate;

	private final ObjectMapper jsonObjectMapper = new ObjectMapper();
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		HttpRequest request = (HttpRequest) e.getMessage();

		if (request.getMethod() == HttpMethod.POST) {

			final LoginRequest loginRequest = jsonObjectMapper.readValue(
					request.getContent().toString(CharsetUtil.UTF_8),
					LoginRequest.class);
			
			final UserLogin userLogin = workerClient.loginUser(loginRequest.getMail(),
					loginRequest.getPassword());

			if (userLogin == null) {
				writeResponse(e, BAD_REQUEST);
				getLogger().warn(
						"Something wrong happens when trying to login the user {}, null is returned...", loginRequest.getMail());
				return;
			} else if (userLogin.isAlreadyLogged()) {
				writeResponse(e, BAD_REQUEST);
				getLogger().warn(
						"User already logged for session {}", loginRequest.getMail());
				return;
			}
			
			if (userLogin.getUserId() != null) {
				
				final String userId = userLogin.getUserId();

				CachedUser cachedUser = getCacheManager().getCachedUser(userId);
				if (cachedUser != null && cachedUser.isLogged()) {
					writeResponse(e, BAD_REQUEST);
					getLogger().warn(
							"User already logged for session {}, userId = {}", loginRequest.getMail(), userLogin.getUserId());
					return;
				}
				
				gameService.enterGame(userId);
				
				getCacheManager().insertFreshlyLoggedUser(userId);
				
				HttpResponse response = new DefaultHttpResponse(HTTP_1_1,
						CREATED);
				setCookie(response, COOKIE_AUTH_NAME, userId);
				
				writeStringToReponse("{\"logged\"=true}", e, response);
				
			} else {
				writeResponse(e, UNAUTHORIZED);
				getLogger().warn(
						"User not found for session {}", loginRequest.getMail());
			}

		} else if (request.getMethod() == HttpMethod.GET) {

			writeHtml(e, loginTemplate);
		} else {

			writeResponse(e, NOT_IMPLEMENTED);
			getLogger().warn("Not implemented");
		}

	}

	private void setCookie(HttpResponse response, String name, String value) {
		CookieEncoder cookieEncoder = new CookieEncoder(true);
		Cookie cookie = new DefaultCookie(name, value);
		cookie.setMaxAge(COOKIE_MAX_AGE);
		cookie.setPath(COOKIE_PATH);
		cookieEncoder.addCookie(cookie);
		response.addHeader(SET_COOKIE, cookieEncoder.encode());
	}

	private static final int COOKIE_MAX_AGE = 60*60*24;
	private static final String COOKIE_PATH = "/";
	
}
