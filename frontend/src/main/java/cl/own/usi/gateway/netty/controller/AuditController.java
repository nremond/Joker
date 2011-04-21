package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.gateway.client.WorkerClient;
import cl.own.usi.model.Game;
import cl.own.usi.service.GameService;

/**
 * Controller that handle the audit requests.
 *
 * @author reynald
 */
@Component
public class AuditController extends AbstractAuthenticateController {

	@Autowired
	private WorkerClient workerClient;

	@Autowired
	private GameService gameService;

	protected static final Pattern URI_PATTERN = Pattern
			.compile("^/api/audit(?:/(\\d+))?\\?(.*)");

	private static final String USER_MAIL_NAME = "user_mail";

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final MessageEvent e) throws Exception {

		final HttpRequest request = (HttpRequest) e.getMessage();

		final Matcher match = URI_PATTERN.matcher(request.getUri());
		if (!match.matches()) {
			writeResponse(e, NOT_FOUND);
			return;
		}

		final Integer questionNumber = match.group(1) == null ? null : Integer
				.valueOf(match.group(1));

		final Map<String, String> queryParameters = parseQueryString(match
				.group(2));

		final String authenticationKey = queryParameters
				.get(AUTHENTIFICATION_KEY_NAME);
		final String userMail = queryParameters.get(USER_MAIL_NAME);

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

		final Game game = gameService.getGame();

		if (game == null) {
			getLogger().error("Audit request received while no game exists!");
			writeResponse(e, BAD_REQUEST);
			return;
		}

		String response = workerClient.getAnswersAsJson(userMail,
				questionNumber, game);

		if (StringUtils.isEmpty(response)) {
			getLogger()
					.error("Audit request for user email {} and question {} returned an empty string",
							userMail, questionNumber);
			writeResponse(e, BAD_REQUEST);
			return;
		}

		writeStringToReponse(response, e);
		return;
	}

}
