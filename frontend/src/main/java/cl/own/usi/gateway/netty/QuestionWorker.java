package cl.own.usi.gateway.netty;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;

import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cl.own.usi.service.GameService;

public class QuestionWorker implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(QuestionWorker.class);
	
	final int questionNumber;
	final int score;
	final MessageEvent e;
	final String questionFirstPart;
	final long timeAtCreation;

	GameService gameService;
	
	public QuestionWorker(int questionNumber, int score, MessageEvent e,
			String questionFirstPart, GameService gameService) {
		this.questionNumber = questionNumber;
		this.score = score;
		this.e = e;
		this.questionFirstPart = questionFirstPart;
		this.timeAtCreation = System.currentTimeMillis();
		this.gameService = gameService;
	}

	public void run() {

		StringBuilder sb = new StringBuilder(questionFirstPart);

		try {
			long alreadyWaitedMili = System.currentTimeMillis()
					- timeAtCreation;
			if (gameService.waitOtherUsers(questionNumber,
					alreadyWaitedMili)) {

				sb.append(",\"score\":").append(score);
				sb.append("}");

				writeStringToReponse(sb.toString(), e, OK);

			} else {

				// time to wait is elapsed, return 400.
				writeResponse(e, BAD_REQUEST);

				logger.warn("Fail to wait on other users for question "
						+ questionNumber + ", maybe long polling timeout");
			}
		} catch (InterruptedException ie) {

			writeResponse(e, BAD_REQUEST);
			logger.warn("Interrupted", ie);

		}
	}

	public int getQuestionNumber() {
		return questionNumber;
	}
}