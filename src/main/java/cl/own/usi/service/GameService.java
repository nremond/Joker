package cl.own.usi.service;

import java.util.List;
import java.util.Map;

import cl.own.usi.model.Game;
import cl.own.usi.model.Question;

public interface GameService {

	boolean insertGame(int usersLimit, int questionTimeLimit, int pollingTimeLimit, List<Map<String, Map<String, Boolean>>> questions);
	
	Question getCurrentQuestion();
	
	boolean waitOtherUsers() throws InterruptedException;
	
	void userEnter();
	
	Game getGame();
	
	long getStartOfCurrentQuestion();
	
}
