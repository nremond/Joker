package cl.own.usi.gateway.netty;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;

import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cl.own.usi.service.GameService;
import cl.own.usi.service.RunnableWithQuestionNumber;

/**
 * Thread that write asynchronously write the question to the channel.
 * 
 * @author bperroud
 *
 */
public class QuestionWorker implements RunnableWithQuestionNumber {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(QuestionWorker.class);

	private final int questionNumber;
	private final MessageEvent e;
	private final byte[] message;

	private GameService gameService;

	public QuestionWorker(final int questionNumber, final MessageEvent e,
			final byte[] message, final GameService gameService) {
		this.questionNumber = questionNumber;
		this.e = e;
		this.message = message;
		this.gameService = gameService;
	}

	public void run() {

		try {

			if (gameService.waitOtherUsers(questionNumber)) {

				writeStringToReponse(message, e, OK);

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