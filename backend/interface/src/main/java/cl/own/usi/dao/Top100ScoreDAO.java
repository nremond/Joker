package cl.own.usi.dao;

import java.util.List;

import cl.own.usi.model.User;

public interface Top100ScoreDAO {
	
	List<User> getTop100();
	
	void setNewScore(User user, int newScore);
	
	void flushUsers();
	
}
