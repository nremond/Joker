package cl.own.usi.dao.impl.mongo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import cl.own.usi.dao.GameDAO;
import cl.own.usi.model.Game;
import cl.own.usi.model.Question;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

/**
 * 
 * @author nicolas
 */
@Repository
public class GameDaoMongoImpl implements GameDAO {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	DB db;

	private final String gameCollectionName = "games";

	// The current game
	private Game game;

	public Game insertGame(int usersLimit, int questionTimeLimit,
			int pollingTimeLimit, List<Question> questions) {

		game = new Game();
		game.setUsersLimit(usersLimit);
		game.setQuestionTimeLimit(questionTimeLimit);
		game.setPollingTimeLimit(pollingTimeLimit);
		game.setQuestions(new ArrayList<Question>(questions));

		if (logger.isDebugEnabled()) {
			logger.debug(ToStringBuilder.reflectionToString(game));
			for (Question q : game.getQuestions())
				logger.debug(ToStringBuilder.reflectionToString(q));
		}
		
		
		DBCollection games = db.getCollection(gameCollectionName);

		BasicDBObject doc = new BasicDBObject();
		doc.put("usersLimit", game.getUsersLimit());
		doc.put("questionTimeLimit", game.getQuestionTimeLimit());
		doc.put("pollingTimeLimit", game.getPollingTimeLimit());
		doc.put("insertionTime", System.currentTimeMillis());

		List<BasicDBObject> dbQuestions = new ArrayList<BasicDBObject>();
		for (Question q : questions) {
			BasicDBObject dbQuestion = new BasicDBObject();
			dbQuestion.put("number", q.getNumber());
			dbQuestion.put("label", q.getLabel());
			dbQuestion.put("correctChoice", q.getCorrectChoice());
			dbQuestion.put("choices", q.getChoices());
			dbQuestions.add(dbQuestion);

		}
		doc.put("questions", dbQuestions);

		games.insert(doc);

		return game;
	}

	public Question getQuestion(int questionNumber) {
		if (game == null || questionNumber < 1
				|| questionNumber > getGame().getQuestions().size()) {
			return null;
		} else {
			return game.getQuestions().get(questionNumber - 1);
		}
	}

	public Game getGame() {
		return game;
	}

}
