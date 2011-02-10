package cl.own.usi.dao.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import cl.own.usi.dao.GameDAO;
import cl.own.usi.model.Game;
import cl.own.usi.model.Question;

@Repository
public class GameDAOImpl implements GameDAO {

	Game game;
	
	public Game insertGame(int usersLimit, int questionTimeLimit, int pollingTimeLimit, 
			List<Question> questions) {
		
		game = new Game();
		
		game.setUsersLimit(usersLimit);
		game.setQuestionTimeLimit(questionTimeLimit);
		game.setPollingTimeLimit(pollingTimeLimit);
		
		game.setQuestions(new ArrayList<Question>(questions));
		
		return game;
	}

	public Question getQuestion(int questionNumber) {
		
		if (game == null || questionNumber < 0 || questionNumber >= game.getQuestions().size()) {
			return null;
		}
		return game.getQuestions().get(questionNumber);
	}

	public Game getGame() {
		return game;
	}

}
