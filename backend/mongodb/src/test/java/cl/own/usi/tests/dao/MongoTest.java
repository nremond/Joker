package cl.own.usi.tests.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;

public class MongoTest {

	private Mongo mongo;
	private DB db;

	private final String dbName = "joker";
	private final String usersCollection = "users";

	@Before
	public void setUp() throws Exception {
		mongo = new Mongo("localhost", 27017);
		db = mongo.getDB(dbName);
	}

	//@Test
	public void welcomeAboardUser() {
		DBCollection dbUsers = db.getCollection(usersCollection);

		BasicDBObject dbUser = new BasicDBObject();
		dbUser.put("email", "tristan@fuckyoudear.com");
		dbUser.put("password", "topsecret!");
		dbUser.put("firstname", "Tristan");
		dbUser.put("lastname", "Doze");
		dbUser.put("score", "3");

		WriteResult wr = dbUsers.insert(dbUser);
		String error = wr.getError();

		// E11000 -> duplicate key
		if (StringUtils.hasText(error) && error.indexOf("E11000") == 0) {
			System.out.println("duplicate");
		}

		DBObject emailIndex = new BasicDBObject("email", 1);

		dbUsers.ensureIndex(emailIndex, "email", true);

		// System.out.println("index created");

	}

	// @Test
	public void showMeTheLastGame() {
		// Game game = new Game();
		//
		// DBCollection games = db.getCollection(collectionName);
		//
		// BasicDBObject orderBy = new BasicDBObject();
		// orderBy.put("insertionTime", -1);
		//
		// DBCursor cursor = games.find().sort(orderBy);
		// if (cursor.hasNext()) {
		// DBObject lastGame = cursor.next();
		//
		// game.setUsersLimit((Integer) lastGame.get("usersLimit"));
		// game.setQuestionTimeLimit((Integer) lastGame
		// .get("questionTimeLimit"));
		// game.setUsersLimit((Integer) lastGame.get("usersLimit"));
		//
		// @SuppressWarnings("unchecked")
		// List<DBObject> dbQuestions = (List<DBObject>) lastGame
		// .get("questions");
		// List<Question> questions = new ArrayList<Question>(
		// dbQuestions.size());
		// for (DBObject dbQuestion : dbQuestions) {
		// Question question = new Question();
		// question.setLabel((String) dbQuestion.get("label"));
		// question.setNumber((Integer) dbQuestion.get("number"));
		// question.setCorrectChoice((Integer) dbQuestion
		// .get("correctChoice"));
		//
		// @SuppressWarnings("unchecked")
		// List<DBObject> dbChoices = (List<DBObject>) dbQuestion
		// .get("choices");
		// List<String> choices = new ArrayList<String>(dbChoices.size());
		// for (DBObject dbChoice : dbChoices) {
		// int number = (Integer) dbChoice.get("number");
		// String choice = (String) dbChoice.get("label");
		// choices.add(number - 1, choice);
		// }
		// question.setChoices(choices);
		//
		// questions.add(question.getNumber() - 1, question);
		// }
		//
		// game.setQuestions(questions);
		// }
		//
		// System.out.println(ToStringBuilder.reflectionToString(game));
		// for (Question q : game.getQuestions())
		// System.out.println(ToStringBuilder.reflectionToString(q));

	}

	@After
	public void tearDown() {
		mongo.close();
	}

}
