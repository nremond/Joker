package cl.own.usi.dao;

import java.util.List;

import cl.own.usi.model.Game;
import cl.own.usi.model.Question;

/**
 * GameDAO
 * 
 * @author bperroud
 * 
 */
public interface GameDAO {

	/**
	 * @return The freshly created game
	 */
	Game insertGame(int usersLimit, int questionTimeLimit,
			int pollingTimeLimit, int synchroTimeLimit, List<Question> questions);

	/**
	 * When a game is created by a node, a new game is persisted. So all nodes
	 * need to refresh their cache.
	 */
	void refreshCache();

	/**
	 * @return The game in cache
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
