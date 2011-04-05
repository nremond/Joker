package cl.own.usi.service;

import java.util.List;
import java.util.Map;

import cl.own.usi.gateway.netty.QuestionWorker;
import cl.own.usi.model.Game;
import cl.own.usi.model.Question;
import cl.own.usi.model.User;

/**
 * Game service.
 *
 * @author bperroud
 *
 */
public interface GameService {

	/**
	 * Create a new {@link Game}. Setup all synchronization stuff, reset
	 * previous game if existing.
	 *
	 * @param usersLimit
	 * @param questionTimeLimit
	 * @param pollingTimeLimit
	 * @param synchroTimeLimit
	 * @param numberOfQuestion
	 * @param questions
	 * @return
	 */
	boolean insertGame(int usersLimit, int questionTimeLimit,
			int pollingTimeLimit, int synchroTimeLimit, int numberOfQuestion,
			List<Map<String, Map<String, Boolean>>> questions);

	/**
	 * Return the current {@link Game}. Possibly <code>null</code> if no game
	 * was previously created.
	 *
	 * @return current game, <code>null</code> if none defined.
	 */
	Game getGame();

	/**
	 * Get given question. Return null if questionNumber does not exist.
	 *
	 * @param questionNumber
	 * @return
	 */
	Question getQuestion(int questionNumber);

	/**
	 * Wait till {@link Game#getUsersLimit()} {@link User} reach the
	 * {@link Game}
	 *
	 * @param questionNumber
	 * @return
	 * @throws InterruptedException
	 */
	boolean waitOtherUsers(int questionNumber) throws InterruptedException;

	/**
	 * Stipulate a {@link User} request the {@link Question}
	 *
	 * @param questionNumber
	 * @return
	 */
	boolean enterGame(String userId);

	boolean validateQuestionToRequest(int questionNumber);

	boolean validateQuestionToAnswer(int questionNumber);

	/**
	 * Stipulate a {@link User} answer the {@link Question}
	 *
	 * @param questionNumber
	 * @return
	 */
	boolean userAnswer(int questionNumber);

	/**
	 * Async sending the question to the user
	 *
	 * @param questionWorker
	 */
	void scheduleQuestionReply(QuestionWorker questionWorker);

	/**
	 * Validates the answer and tells if the answer is correct or not
	 *
	 * @param questionNumber
	 * @param answer
	 * @return
	 */
	boolean isAnswerCorrect(int questionNumber, Integer answer);

	/**
	 * Tells if the ranking request is allowed or not.
	 *
	 * @return
	 */
	boolean isRankingRequestAllowed();

	String getTop100AsString();
}
