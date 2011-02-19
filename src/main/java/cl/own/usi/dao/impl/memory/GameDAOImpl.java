package cl.own.usi.dao.impl.memory;

import java.util.ArrayList;
import java.util.List;

import cl.own.usi.dao.GameDAO;
import cl.own.usi.model.Game;
import cl.own.usi.model.Question;

//@Repository
public class GameDAOImpl implements GameDAO {

	private Game game;

	public Game insertGame(int usersLimit, int questionTimeLimit,
			int pollingTimeLimit, List<Question> questions) {

		game = new Game();

		game.setUsersLimit(usersLimit);
		game.setQuestionTimeLimit(questionTimeLimit);
		game.setPollingTimeLimit(pollingTimeLimit);

		game.setQuestions(new ArrayList<Question>(questions));

		return game;
	}

	public Question getQuestion(int questionNumber) {
		if (game == null || questionNumber < 1
				|| questionNumber > getGame().getQuestions().size()) {
			return null;
		} else {
			return getGame().getQuestions().get(questionNumber - 1);
		}
	}

	public Game getGame() {
		return game;
	}

}
