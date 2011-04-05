package cl.own.usi.gateway.netty;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;

import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cl.own.usi.service.GameService;

/**
 * Thread that write asynchronously write the question to the channel.
 * 
 * @author bperroud
 *
 */
public class QuestionWorker implements Runnable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(QuestionWorker.class);

	private final int questionNumber;
	private final int score;
	private final MessageEvent e;
	private final String questionFirstPart;

	private GameService gameService;

	public QuestionWorker(final int questionNumber, final int score, final MessageEvent e,
			final String questionFirstPart, final GameService gameService) {
		this.questionNumber = questionNumber;
		this.score = score;
		this.e = e;
		this.questionFirstPart = questionFirstPart;
		this.gameService = gameService;
	}

	public void run() {

		StringBuilder sb = new StringBuilder(questionFirstPart);

		try {

			if (gameService.waitOtherUsers(questionNumber)) {

				sb.append(",\"score\":").append(score);
				sb.append("}");

				writeStringToReponse(sb.toString(), e, OK);

			} else {

				// time to wait is elapsed, return 400.
				writeResponse(e, BAD_REQUEST);

				LOGGER.warn("Fail to wait on other users for question "
						+ questionNumber + ", maybe long polling timeout");
			}
		} catch (InterruptedException ie) {

			writeResponse(e, BAD_REQUEST);
			LOGGER.warn("Interrupted", ie);

		}
	}

	public int getQuestionNumber() {
		return questionNumber;
	}
}