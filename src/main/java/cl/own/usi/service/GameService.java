package cl.own.usi.service;

import java.util.List;
import java.util.Map;

import cl.own.usi.model.Question;

public interface GameService {

	boolean insertGame(int usersLimit, int questionTimeLimit, int pollingTimeLimit, List<Map<String, Map<String, Boolean>>> questions);
	
	Question getQuestion(int questionNumber);
	Question getCurrentQuestion();
	
	boolean waitOtherUsers(int questionNumber) throws InterruptedException;
	
	boolean userEnter(int questionNumber);
	
	boolean userAnswer(int questionNumber);
	
}
