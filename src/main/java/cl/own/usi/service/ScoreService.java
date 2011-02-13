package cl.own.usi.service;

import java.util.List;

import cl.own.usi.model.Question;
import cl.own.usi.model.User;

public interface ScoreService {

	int updateScore(Question question, User user, boolean answerCorrect);
	
	List<User> getTop100();
	
	List<User> get50Before(User user);
	
	List<User> get50After(User user);
}
