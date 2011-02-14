package cl.own.usi.service;

import java.util.List;
import java.util.Map;

import cl.own.usi.model.Game;
import cl.own.usi.model.Question;
import cl.own.usi.model.User;

public interface GameService {

	/**
	 * Create a new {@link Game}. Setup all synchronization stuff, reset previous game if existing.
	 * 
	 * @param usersLimit
	 * @param questionTimeLimit
	 * @param pollingTimeLimit
	 * @param questions
	 * @return
	 */
	boolean insertGame(int usersLimit, int questionTimeLimit, int pollingTimeLimit, List<Map<String, Map<String, Boolean>>> questions);
	
	/**
	 * Get given question. Return null if questionNumber does not exist.
	 * 
	 * @param questionNumber
	 * @return
	 */
	Question getQuestion(int questionNumber);
	
	/**
	 * Wait till {@link Game#getUsersLimit()} {@link User} reach the {@link Game}
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
	boolean userEnter(int questionNumber);
	
	/**
	 * Stipulate a {@link User} answer the {@link Question}
	 * 
	 * @param questionNumber
	 * @return
	 */
	boolean userAnswer(int questionNumber);
	
}
