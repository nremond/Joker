package cl.own.usi.service;

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
	boolean insertUser(String email, String password, String firstname, String lastname);
	
	/**
	 * Login a {@link User}
	 * 
	 * @param email
	 * @param password
	 * @return
	 */
	String login(String email, String password);
	
	/**
	 * Request a {@link Question}
	 * 
	 * @param user
	 * @param questionNumber
	 * @return
	 */
	void insertRequest(User user, int questionNumber);
	
	/**
	 * Answer a {@link Question}
	 * 
	 * @param user
	 * @param questionNumber
	 * @param answer
	 * @return
	 */
	void insertAnswer(User user, int questionNumber, Integer answer);
	
	/**
	 * Logout
	 * 
	 * @param userId
	 * @return
	 */
	boolean logout(String userId);
	
	/**
	 * Map the "userId" returned by a previous {@link #login(String, String)} call to an existing {@link User}
	 * 
	 * @param userId
	 * @return
	 */
	User getUserFromUserId(String userId);
	
	/**
	 * Remove all {@link User}s
	 */
	void flushUsers();
	
}
