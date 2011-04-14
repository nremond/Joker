package cl.own.usi.dao;

import java.util.List;

import cl.own.usi.exception.UserAlreadyLoggedException;
import cl.own.usi.model.Answer;
import cl.own.usi.model.User;

public interface UserDAO {

	/**
	 * Insert a new user in the DB
	 *
	 * @param user
	 *            to insert
	 * @return true if the user has been inserted, false if a user with the same
	 *         email has been inserted before
	 */
	boolean insertUser(User user);

	// TODO Remove this method, absolutly useless !!
	User getUserById(String userId);

	void insertRequest(String userId, int questionNumber);

	void insertAnswer(final Answer answer);

	List<Answer> getAnswers(final String userId);

	List<Answer> getAnswersByEmail(final String userEmail);

	/**
	 * Log the user in
	 *
	 * @return The userId is the credentials are good, null otherwise
	 */
	String login(String email, String password) throws UserAlreadyLoggedException;

	void logout(String userId);

	void flushUsers();

	void gameCreated();
	
}
