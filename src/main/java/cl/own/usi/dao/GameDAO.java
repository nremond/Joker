package cl.own.usi.dao;

import java.util.List;

import cl.own.usi.model.Game;
import cl.own.usi.model.Question;

public interface GameDAO {

	Game insertGame(int usersLimit, int questionTimeLimit, int pollingTimeLimit, 
			List<Question> questions);
	
	Game getGame();
	
	/**
	 * Get the given question.
	 * 
	 * @param questionNumber
	 * @return
	 */
	Question getQuestion(int questionNumber);
	
}
