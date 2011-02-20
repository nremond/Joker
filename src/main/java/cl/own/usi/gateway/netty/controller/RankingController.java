package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;

import java.util.List;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.gateway.client.WorkerClient;
import cl.own.usi.gateway.client.WorkerClient.UserAndScore;
import cl.own.usi.gateway.client.WorkerClient.UserInfoAndScore;

@Component
public class RankingController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private WorkerClient workerClient;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();

		String userId = getCookie(request, COOKIE_AUTH_NAME);

		if (userId == null) {
			writeResponse(e, UNAUTHORIZED);
			logger.info("User not authorized");
		} else {

			UserAndScore userAndScore = workerClient
					.validateUserAndGetScore(userId);

			if (userAndScore.userId == null) {
				writeResponse(e, BAD_REQUEST);
				logger.info("Invalid userId " + userId);
			} else {

				StringBuilder sb = new StringBuilder("{");

				sb.append(" \"my_score\" : ").append(userAndScore.score)
						.append(", ");

				sb.append(" \"top_scores\" : { ");
				List<UserInfoAndScore> topUsers = workerClient.getTop100();
				appendUsersScores(topUsers, sb);
				sb.append(" }, ");

				sb.append(" \"before_me\" : { ");
				List<UserInfoAndScore> beforeScores = workerClient
						.get50Before(userId);
				appendUsersScores(beforeScores, sb);
				sb.append(" }, ");

				sb.append(" \"after_me\" : { ");
				List<UserInfoAndScore> afterScores = workerClient
						.get50After(userId);
				appendUsersScores(afterScores, sb);
				sb.append(" } ");

				sb.append(" } ");

				writeStringToReponse(sb.toString(), e, OK);
			}

		}
	}

	private void appendUsersScores(List<UserInfoAndScore> users,
			StringBuilder sb) {
		StringBuilder topScoresMail = new StringBuilder("\"mail\" : [ ");
		StringBuilder topScoresScores = new StringBuilder("\"scores\" : [ ");
		StringBuilder topScoresFirstName = new StringBuilder(
				"\"firstname\" : [ ");
		StringBuilder topScoresLastname = new StringBuilder("\"lastname\" : [ ");
		boolean first = true;
		for (UserInfoAndScore user : users) {
			if (!first) {
				topScoresMail.append(",");
			}
			topScoresMail.append("\"").append(user.email).append("\"");
			if (!first) {
				topScoresScores.append(",");
			}
			topScoresScores.append(user.score);
			if (!first) {
				topScoresFirstName.append(",");
			}
			topScoresFirstName.append("\"").append(user.firstname).append("\"");
			if (!first) {
				topScoresLastname.append(",");
			}
			topScoresLastname.append("\"").append(user.lastname).append("\"");
			first = false;
		}
		sb.append(topScoresMail).append(" ] , ");
		sb.append(topScoresScores).append(" ] , ");
		sb.append(topScoresFirstName).append(" ] , ");
		sb.append(topScoresLastname).append(" ] ");
	}

}
