package cl.own.usi.dao;

import java.util.List;

import cl.own.usi.model.User;

public interface ScoreDAO {

	/**
	 * Return the Users having the higher score.
	 * 
	 * @param limit
	 * @return
	 */
	List<User> getTop(int limit);

	/**
	 * Return the Users having a score just better than the given User.
	 * 
	 * @param user
	 * @param limit
	 * @return
	 */
	List<User> getBefore(User user, int limit);

	/**
	 * Return the Users having a score just worst than the given User.
	 * 
	 * @param user
	 * @param limit
	 * @return
	 */
	List<User> getAfter(User user, int limit);


	/**
	 * add the bonus to the current score, increase the score by one and return the new score
	 * @param userId
	 * @param questionNumber
	 * @param questionValue
	 * @return current score = score + bonus
	 */
	int setGoodAnswer(String userId, int questionNumber, int questionValue);
	
	/**
	 * Reset user bonus and return his current score
	 * @param userId
	 * @param questionNumber
	 * @return current user score
	 */
	int setBadAnswer(String userId, int questionNumber);

	void flushUsers();

	void computeRankings();
	
}
