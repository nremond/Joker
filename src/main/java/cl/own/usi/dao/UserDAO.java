package cl.own.usi.dao;

import cl.own.usi.model.User;

public interface UserDAO {

	/**
	 * Insert a new User.
	 * 
	 * @param user
	 * @return
	 */
	boolean insertUser(User user);
	
	/**
	 * Log a User in and return it.
	 * 
	 * @param email
	 * @param password
	 * @return
	 */
	User userLogin(String email, String password);
	
	/**
	 * Store the answer of the User.
	 * 
	 * @param user
	 * @param questionNumber
	 * @param answer
	 * @return
	 */
	boolean insertAnswer(User user, int questionNumber, String answer);
	
	/**
	 * Log out the User.
	 * 
	 * @param user
	 */
	void logout(User user);
	
}
