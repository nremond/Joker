package cl.own.usi.service;

import cl.own.usi.model.User;

public interface UserService {
	
	boolean insertUser(String email, String password, String firstname, String lastname);
	
	String login(String email, String password);
	
	boolean insertAnswer(String userId, Integer answer);
	
	boolean logout(String userId);
	
	User getUserFromUserId(String userId);
	
	void flushUsers();
	
}
