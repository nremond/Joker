package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.cache.CacheManager;
import cl.own.usi.cache.CachedUser;
import cl.own.usi.service.CachedScoreService;
import cl.own.usi.service.GameService;

/**
 * Controller that return the rank and scores
 *
 * @author bperroud
 * @author nicolas
 */
@Component
public class RankingController extends AbstractController {

	private static final int NUMBER_BEFORE_AFTER = 5;

	@Autowired
	private GameService gameService;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private CachedScoreService cachedScoreService;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();

		String userId = getCookie(request, COOKIE_AUTH_NAME);

		if (userId == null) {
			writeResponse(e, UNAUTHORIZED);
			getLogger().info("User not authorized");
		} else {

			if (!gameService.isRankingRequestAllowed()) {
				writeResponse(e, BAD_REQUEST);
			} else {

				final CachedUser cachedUser = cacheManager.loadUser(userId);

				if (cachedUser == null) {
					writeResponse(e, UNAUTHORIZED);
					getLogger().info("Invalid userId {}", userId);
				} else {

					if (cachedUser.setRankingRequested()) {
						gameService.requestRanking(userId);
					}

					StringBuilder sb = new StringBuilder("{");

					sb.append("\"score\":\"")
							.append(cachedUser.getScore()).append("\",");

					sb.append("\"top_scores\":{")
					.append(gameService.getTop100AsString())
					.append("},");

//					BeforeAndAfterScores beforeAndAfterScores = workerClient.get50BeforeAnd50After(userId);

					sb.append("\"before\":{");
					sb.append(cachedScoreService.getBefore(userId, NUMBER_BEFORE_AFTER));
//					ScoresHelper.appendUsersScores(beforeAndAfterScores.getScoresBefore(), sb);
					sb.append("},");

					sb.append("\"after\":{");
					sb.append(cachedScoreService.getAfter(userId, NUMBER_BEFORE_AFTER));
//					ScoresHelper.appendUsersScores(beforeAndAfterScores.getScoresAfter(), sb);
					sb.append("}");

					sb.append("}");

					writeStringToReponse(sb.toString(), e, OK);
				}
			}
		}
	}

}
