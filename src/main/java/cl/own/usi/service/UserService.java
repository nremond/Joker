package cl.own.usi.service;

import cl.own.usi.model.User;

public interface UserService {
	
	boolean insertUser(String email, String password, String firstname, String lastname);
	
	String login(String email, String password);
	
	boolean insertRequest(User user, int questionNumber);
	boolean insertAnswer(User user, int questionNumber, Integer answer);
	
	boolean logout(String userId);
	
	User getUserFromUserId(String userId);
	
	void flushUsers();
	
	boolean isQuestionRequestAllowed(User user, int questionNumber);
	boolean isQuestionResponseAllowed(User user, int questionNumber);
	
}
