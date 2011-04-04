package cl.own.usi.service;

import java.util.List;

import cl.own.usi.model.AuditAnswer;
import cl.own.usi.model.AuditAnswers;
import cl.own.usi.model.User;

public interface UserService {

	/**
	 * Insert a new {@link User}
	 *
	 * @param email
	 * @param password
	 * @param firstname
	 * @param lastname
	 * @return
	 */
	boolean insertUser(String email, String password, String firstname,
			String lastname);

	/**
	 * Login a {@link User}
	 *
	 * @param email
	 * @param password
	 * @return userId
	 */
	String login(String email, String password);

	/**
	 * Request a {@link Question}
	 *
	 * @param userId
	 * @param questionNumber
	 * @return
	 */
	void insertRequest(String userId, int questionNumber);

	/**
	 * Answer a {@link Question}
	 *
	 * @param userId
	 * @param questionNumber
	 * @param answer
	 * @return
	 */
	void insertAnswer(String userId, int questionNumber, Integer answer);

	/**
	 * Logout
	 *
	 * @param userId
	 * @return
	 */
	boolean logout(String userId);

	/**
	 * Map the "userId" returned by a previous {@link #login(String, String)}
	 * call to an existing {@link User}
	 *
	 * @TODO remove this method
	 * @param userId
	 * @return
	 */
	User getUserFromUserId(String userId);

	/**
	 * Remove all {@link User}s
	 */
	void flushUsers();

	AuditAnswers getAuditAnswers(String userEmail, List<Integer> goodAnswers);

	AuditAnswer getAuditAnswerFor(String userEmail, int questionNumber,
			String question, int goodAnswer);

}
