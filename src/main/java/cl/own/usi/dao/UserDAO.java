package cl.own.usi.dao;

import cl.own.usi.model.User;

public interface UserDAO {

	boolean insertUser(User user);
	
	User getUserById(String userId);
	
	boolean insertAnswer(User user, int questionNumber, String answer);
	
	void logout(User user);
	
	void flushUsers();
	
}
