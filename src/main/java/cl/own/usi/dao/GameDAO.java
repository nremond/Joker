package cl.own.usi.dao;

import java.util.List;

import cl.own.usi.model.Question;

public interface GameDAO {

	/**
	 * Insert a new game.
	 * 
	 * @param name
	 * @param userLimit
	 * @param timeLimit
	 * @param questions
	 * @return
	 */
	boolean insertGame(String name, int userLimit, int timeLimit, List<Question> questions, int longPollingMaxDuration);
	
	/**
	 * Get the given question.
	 * 
	 * @param questionNumber
	 * @return
	 */
	Question getQuestion(int questionNumber);
	
}
