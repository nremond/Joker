package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_0;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

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

			String userId = workerClient.loginUser(loginRequest.getMail(),
					loginRequest.getPassword());

			if (userId != null) {

				gameService.enterGame(userId);

				HttpResponse response = new DefaultHttpResponse(HTTP_1_0,
						CREATED);
				setCookie(response, COOKIE_AUTH_NAME, userId);
				ChannelFuture future = e.getChannel().write(response);
				future.addListener(ChannelFutureListener.CLOSE);
			} else {
				writeResponse(e, BAD_REQUEST);
				getLogger().warn(
						"User not found for session " + loginRequest.getMail());
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
		cookieEncoder.addCookie(name, value);
		response.addHeader(SET_COOKIE, cookieEncoder.encode());
	}

}
