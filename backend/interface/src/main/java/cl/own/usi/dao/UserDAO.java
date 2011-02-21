package cl.own.usi.dao;

import java.util.List;

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

	User getUserById(String userId);

	void insertRequest(User user, int questionNumber);

	void insertAnswer(User user, Answer answer);

	List<Answer> getAnswers(User user);

	String login(String email, String password);

	void logout(User user);

	void flushUsers();

}
