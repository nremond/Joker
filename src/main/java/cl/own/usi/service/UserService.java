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
	boolean insertRequest(User user, int questionNumber);
	
	/**
	 * Answer a {@link Question}
	 * 
	 * @param user
	 * @param questionNumber
	 * @param answer
	 * @return
	 */
	boolean insertAnswer(User user, int questionNumber, Integer answer);
	
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
	
	/**
	 * Check if the {@link User} is allowed to request the given {@link Question}.
	 * 
	 * @param user
	 * @param questionNumber
	 * @return
	 */
	boolean isQuestionRequestAllowed(User user, int questionNumber);
	
	/**
	 * Check if the {@link User} is allowed to answer the given {@link Question}.
	 * 
	 * @param user
	 * @param questionNumber
	 * @return
	 */
	boolean isQuestionResponseAllowed(User user, int questionNumber);
	
}
