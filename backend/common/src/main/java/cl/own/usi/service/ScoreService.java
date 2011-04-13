package cl.own.usi.service;

import java.util.List;

import cl.own.usi.model.User;

public interface ScoreService {

	/**
	 * Update the score of a {@link User}
	 *
	 * @param question
	 * @param user
	 * @param answerCorrect
	 * @return
	 */
	int updateScore(int questionNumber, int questionValue, int answer,
			String userId, boolean answerCorrect);

	/**
	 * Return the 100 top {@link User}
	 *
	 * @return
	 */
	List<User> getTop100();

	/**
	 * Return the 50 {@link User} ranked just before the given {@link User}
	 *
	 * @param user
	 * @return
	 */
	List<User> get50Before(User user);

	/**
	 * Return the 50 {@link User} ranked just after the given {@link User}
	 *
	 * @param user
	 * @return
	 */
	List<User> get50After(User user);

	/**
	 * Compute rankings for all {@link User}s (if needed)
	 */
	void computeRankings();

}
