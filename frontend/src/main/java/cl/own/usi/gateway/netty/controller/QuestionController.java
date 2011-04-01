package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.cache.CachedUser;
import cl.own.usi.gateway.client.UserAndScore;
import cl.own.usi.gateway.client.WorkerClient;
import cl.own.usi.gateway.netty.QuestionWorker;
import cl.own.usi.model.Question;
import cl.own.usi.service.GameService;

/**
 * Controller that send asynchronously the {@link Question}
 * 
 * @author bperroud
 * @author nicolas
 */
@Component
public class QuestionController extends AbstractController {

	public static final String URI_QUESTION = "/question/";
	private static final int URI_QUESTION_LENGTH = URI_QUESTION.length();

	@Autowired
	private GameService gameService;

	@Autowired
	private WorkerClient workerClient;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();

		String uri = request.getUri();
		uri = uri.substring(URI_API_LENGTH);

		if (request.getMethod() == HttpMethod.GET) {
			final String userId = getCookie(request, COOKIE_AUTH_NAME);

			if (userId == null) {
				writeResponse(e, UNAUTHORIZED);
				getLogger().info("User not authorized");
			} else {
				try {
					final int questionNumber = Integer.parseInt(uri
							.substring(URI_QUESTION_LENGTH));

					if (!gameService.validateQuestionToRequest(questionNumber)) {
						writeResponse(e, BAD_REQUEST);
						getLogger().info(
								"Invalid question number " + questionNumber);
					} else {

						final CachedUser cachedUser = getCacheManager().loadUser(userId);
						
						if (cachedUser == null) {
							writeResponse(e, UNAUTHORIZED);
							getLogger().info("User not authorized");
							return;
//						}
//						
//						UserAndScore userAndScore = workerClient
//								.validateUserAndInsertQuestionRequest(userId,
//										questionNumber);
//
//						if (userAndScore == null
//								|| userAndScore.getUserId() == null) {
//							writeResponse(e, UNAUTHORIZED);
//							getLogger().info("Invalid userId {}", userId);
						} else {
							getLogger().debug("Get Question {} for user {}",
									questionNumber, userId);

							final Question question = gameService
									.getQuestion(questionNumber);

							final StringBuilder sb = new StringBuilder("{");
							sb.append("\"question\":\"")
									.append(question.getLabel()).append("\"");
							int i = 0;
							for (String answer : question.getChoices()) {
								i++;
								sb.append(",\"answer_").append(i)
										.append("\":\"").append(answer)
										.append("\"");
							}

							gameService
									.scheduleQuestionReply(new QuestionWorker(
											questionNumber, cachedUser.getScore(), e, sb
													.toString(), gameService));
						}
					}

				} catch (NumberFormatException exception) {
					writeResponse(e, BAD_REQUEST);
					getLogger().warn("NumberFormatException", exception);
				}
			}
		} else {
			writeResponse(e, NOT_IMPLEMENTED);
			getLogger().info("Wrong method");
		}

	}

}
