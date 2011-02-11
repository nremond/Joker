package cl.own.usi.dao;

import java.util.List;

import cl.own.usi.model.Answer;
import cl.own.usi.model.User;

public interface UserDAO {

	boolean insertUser(User user);
	
	User getUserById(String userId);
	
	void insertAnswer(User user, Answer answer);
	
	List<Answer> getAnswers(User user);
	
	String login(String email, String password);
	
	void logout(User user);
	
	void flushUsers();
	
}
