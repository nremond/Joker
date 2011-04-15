 package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.gateway.client.WorkerClient;

/**
 * Controller that gives the score to an admin
 *
 * @author nicolas
 */
@Component
public class ScoreController extends AbstractAuthenticateController {

	private static final String USER_MAIL_NAME = "user_mail";

	@Autowired
	private WorkerClient workerClient;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();

		if (request.getMethod() == HttpMethod.GET) {

			final Map<String, String> queryParameters = parseQueryString(request
					.getUri());


			final String authenticationKey = queryParameters
					.get(AUTHENTIFICATION_KEY_NAME);
			final String userMail = queryParameters.get(USER_MAIL_NAME);


			System.out.println("userMail="+userMail+"authenticationKey="+authenticationKey);

			if (StringUtils.isEmpty(authenticationKey)
					|| StringUtils.isEmpty(userMail)) {

				getLogger().debug("Missing key or email in request '{}'",
						request.getUri());
				writeResponse(e, NOT_FOUND);
				return;
			}

			if (!isAuthenticationKeyValid(authenticationKey)) {
				getLogger().error("Bad authentification key received: '{}'",
						authenticationKey);
				writeResponse(e, FORBIDDEN);
				return;
			}

			String response = workerClient.getScoreAsJson(userMail);

			if (StringUtils.isEmpty(response)) {
				getLogger()
						.error("Score request for user email {} returned an empty string",
								userMail);
				writeResponse(e, BAD_REQUEST);
				return;
			}

			writeStringToReponse(response, e);
			return;

		} else {
			writeResponse(e, NOT_IMPLEMENTED);
			getLogger().info("Wrong method.");
		}

	}

}
