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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

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

		if (logger.isInfoEnabled()) {
			logger.info(ToStringBuilder.reflectionToString(game));
			for (Question q : game.getQuestions())
				logger.info(ToStringBuilder.reflectionToString(q));
		}

		DBCollection games = db.getCollection(gameCollectionName);

		BasicDBObject doc = new BasicDBObject();
		doc.put("usersLimit", game.getUsersLimit());
		doc.put("questionTimeLimit", game.getQuestionTimeLimit());
		doc.put("pollingTimeLimit", game.getPollingTimeLimit());
		doc.put("insertionTime", System.currentTimeMillis());

		List<BasicDBObject> dbQuestions = new ArrayList<BasicDBObject>(
				questions.size());
		for (Question q : questions) {
			BasicDBObject dbQuestion = new BasicDBObject();
			dbQuestion.put("number", q.getNumber());
			dbQuestion.put("label", q.getLabel());
			dbQuestion.put("correctChoice", q.getCorrectChoice());
			List<BasicDBObject> dbChoices = new ArrayList<BasicDBObject>(q
					.getChoices().size());
			int index = 1;
			for (String choice : q.getChoices()) {
				BasicDBObject dbChoice = new BasicDBObject();
				dbChoice.put("number", index++);
				dbChoice.put("label", choice);
				dbChoices.add(dbChoice);
			}

			dbQuestion.put("choices", dbChoices);
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

	@Override
	public void refreshCache() {

		DBCollection games = db.getCollection(gameCollectionName);
		
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("insertionTime", -1);

		DBCursor cursor = games.find().sort(orderBy);
		if (cursor.hasNext()) {
			DBObject lastGame = cursor.next();
			
			game.setUsersLimit((Integer)lastGame.get("usersLimit")); 
			game.setQuestionTimeLimit((Integer)lastGame.get("questionTimeLimit")); 
			game.setUsersLimit((Integer)lastGame.get("usersLimit")); 
			
			@SuppressWarnings("unchecked")
			List<DBObject> dbQuestions = (List<DBObject>) lastGame.get("questions");
			List<Question> questions = new ArrayList<Question>(dbQuestions.size());
			for(DBObject dbQuestion : dbQuestions) {
				Question question = new Question();
				question.setLabel((String)dbQuestion.get("label"));
				question.setNumber((Integer)dbQuestion.get("number"));
				question.setCorrectChoice((Integer)dbQuestion.get("correctChoice"));
				
				@SuppressWarnings("unchecked")
				List<DBObject> dbChoices = (List<DBObject>) dbQuestion.get("choices");
				List<String> choices = new ArrayList<String>(dbChoices.size());
				for(DBObject dbChoice : dbChoices){
					int number = (Integer)dbChoice.get("number");
					String choice = (String)dbChoice.get("label");
					choices.add(number-1, choice);
				}				
				question.setChoices(choices);

				questions.add(question.getNumber()-1,question);
			}
			
			game.setQuestions(questions);
		}

		// Print the game that is in the cache
		if (logger.isInfoEnabled()) {
			logger.info("let's play this game now : " + ToStringBuilder.reflectionToString(game));
			for (Question q : game.getQuestions())
				logger.info(ToStringBuilder.reflectionToString(q));
		}
		
	}
}
