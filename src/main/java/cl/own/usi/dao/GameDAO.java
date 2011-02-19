package cl.own.usi.dao;

import java.util.List;

import cl.own.usi.model.Game;
import cl.own.usi.model.Question;

public interface GameDAO {

	/**
	 * @return The freshly created game
	 */
	Game insertGame(int usersLimit, int questionTimeLimit, int pollingTimeLimit, 
			List<Question> questions);
	
	
	/**
	 * @return The last created game
	 */
	Game getGame();
	
	/**
	 * Get the given question.
	 * 
	 * @param questionNumber
	 * @return
	 */
	Question getQuestion(int questionNumber);
	
}
