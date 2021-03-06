package cl.own.usi.dao.impl.mongo;

import static cl.own.usi.dao.impl.mongo.DaoHelper.bonusField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.isLoggedField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.namesEmailField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.orderByScoreNames;
import static cl.own.usi.dao.impl.mongo.DaoHelper.questionFieldPrefix;
import static cl.own.usi.dao.impl.mongo.DaoHelper.scoreField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.userIdField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.usersCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import cl.own.usi.dao.ScoreDAO;
import cl.own.usi.model.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

@Repository
public class ScoreDAOMongoImpl implements ScoreDAO, InitializingBean {

	@Autowired
	private DB db;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ScoreDAOMongoImpl.class);

	private int threadsNumber = 4;
	
	private ExecutorService writeExecutor;
	
	// Natural order operator
	private static enum OrderOperator {
		GreatherThan("$gt"), LesserThan("$lt");

		private final String mongoOperator;

		OrderOperator(String mongoOperator) {
			this.mongoOperator = mongoOperator;
		}

		@Override
		public String toString() {
			return mongoOperator;
		}
	}

	private final static DBObject rankingFieldsToFetch = new BasicDBObject()
			.append(userIdField, 1).append(namesEmailField, 1)
			.append(scoreField, 1);

	private int getThreadsNumber() {
		return threadsNumber;
	}
	
	@Value(value = "${backend.mongo.threadsNumber:4}")
	private void setThreadsNumber(int threadsNumber) {
		this.threadsNumber = threadsNumber;
	}
	
	private List<User> getUsers(DBObject query, int limit) {

		DBCollection dbUsers = db.getCollection(usersCollection);

		DBCursor dbCursor = dbUsers.find(query, rankingFieldsToFetch)
				.limit(limit).sort(orderByScoreNames);

		List<User> users = new ArrayList<User>(limit);
		while (dbCursor.hasNext()) {
			DBObject dbUser = dbCursor.next();
			User user = DaoHelper.fromDBObject(dbUser);
			users.add(user);
		}
		return users;
	}

	@Override
	public List<User> getTop(int limit) {
		DBObject query = new BasicDBObject();
		query.put(isLoggedField, Boolean.TRUE);
		return getUsers(query, limit);
	}

	private DBObject generateOrderQuery(final OrderOperator orderOp,
			final User user) {
		// eg with gt :
		// (score > current_score)
		// || (score == current_score && names < current_names)

		final OrderOperator oppositeOrder = (orderOp == OrderOperator.GreatherThan) ? OrderOperator.LesserThan
				: OrderOperator.GreatherThan;

		// 1st criteria
		DBObject criteria1 = new BasicDBObject();
		DBObject scoreGT = new BasicDBObject();
		scoreGT.put(orderOp.toString(), user.getScore());
		criteria1.put(scoreField, scoreGT);

		// 2nd criteria
		DBObject criteria2 = new BasicDBObject();
		criteria2.put(scoreField, user.getScore());

		DBObject namesGT = new BasicDBObject();
		namesGT.put(oppositeOrder.toString(), DaoHelper.getNames(user));
		criteria2.put(namesEmailField, namesGT);

		// Full query
		DBObject query = new BasicDBObject();
		query.put("$or", Arrays.asList(criteria1, criteria2));
		query.put(isLoggedField, Boolean.TRUE);

		return query;
	}

	@Override
	public List<User> getBefore(User user, int limit) {

		DBObject query = generateOrderQuery(OrderOperator.GreatherThan, user);
		final List<User> users = getUsers(query, limit);

		LOGGER.debug("get the {} users before {} : got {} results ",
				new Object[] { limit, user.getUserId(), users.size() });

		return users;
	}

	@Override
	public List<User> getAfter(User user, int limit) {

		DBObject query = generateOrderQuery(OrderOperator.LesserThan, user);
		final List<User> users = getUsers(query, limit);

		LOGGER.debug("get the {} users after {} : got {} results ",
				new Object[] { limit, user.getUserId(), users.size() });

		return users;
	}

	@Override
	public int setBadAnswer(final String userId, final int questionNumber,
			final int answer) {

		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbId = new BasicDBObject();
		dbId.put(userIdField, userId);

		DBObject scoreBonusFieldsToFetch = new BasicDBObject();
		scoreBonusFieldsToFetch.put(scoreField, 1);

		long starttime = System.currentTimeMillis();
		
		DBObject dbUser = dbUsers.findOne(dbId, scoreBonusFieldsToFetch);
		
		long loadtime = System.currentTimeMillis() - starttime;
		if (loadtime > 350L) {
			LOGGER.warn("Loading {} took {} ms", userId, loadtime);
		}
		
		final int score = (Integer) dbUser.get(scoreField);
		
		DBObject dbUpdate = new BasicDBObject();
		// Reset the bonus
		dbUpdate.put(bonusField, Integer.valueOf(0));
		// Save the answer
		dbUpdate.put(questionFieldPrefix + questionNumber,
				Integer.valueOf(answer));

		writeExecutor.execute(new AsyncWriter(dbId, dbUpdate));
		
		LOGGER.debug("setBadAnswer for user {} whose score is now {}", userId,
				score);

		return score;
	}

	@Override
	public int setGoodAnswer(final String userId, final int questionNumber,
			final int questionValue, final int answer) {

		DBCollection dbUsers = db.getCollection(usersCollection);

		// Get the current score and bonus
		DBObject dbId = new BasicDBObject();
		dbId.put(userIdField, userId);

		DBObject scoreBonusFieldsToFetch = new BasicDBObject();
		scoreBonusFieldsToFetch.put(scoreField, 1);
		scoreBonusFieldsToFetch.put(bonusField, 1);

		String previousQuestion = questionFieldPrefix + (questionNumber - 1);
		// fetch the previous question
		if (questionNumber > 1) {
			scoreBonusFieldsToFetch.put(previousQuestion, 1);
		}

		long starttime = System.currentTimeMillis();

		DBObject dbUser = dbUsers.findOne(dbId, scoreBonusFieldsToFetch);

		long loadtime = System.currentTimeMillis() - starttime;
		if (loadtime > 350L) {
			LOGGER.warn("Loading {} took {} ms", userId, loadtime);
		}
		
		int bonus = (Integer) dbUser.get(bonusField);
		final int score = (Integer) dbUser.get(scoreField);

		if (questionNumber > 1) {
			// if the user hasn't answer to the previous question, his bonus is
			// reset
			final int previousQuestionValue = (Integer) dbUser
					.get(previousQuestion);
			if (previousQuestionValue < 0) {
				bonus = 0;
			}
		}

		final int newScore = score + bonus + questionValue;
		final int newBonus = bonus + 1;

		DBObject dbUpdate = new BasicDBObject();
		dbUpdate.put(scoreField, Integer.valueOf(newScore));
		dbUpdate.put(bonusField, Integer.valueOf(newBonus));
		// Save the answer
		dbUpdate.put(questionFieldPrefix + questionNumber,
				Integer.valueOf(answer));

		writeExecutor.execute(new AsyncWriter(dbId, dbUpdate));

		LOGGER.debug(
				"setGoodAnswer for user {} whose previous score was {} and is now {}",
				new Object[] { userId, score, newScore });

		return newScore;
	}

	@Override
	public void flushUsers() {
		// TODO Auto-generated method stub
	}

	@Override
	public void gameEnded() {
		// not needed in this implementation
	}

	private class AsyncWriter implements Runnable {

		private final DBObject id;
		private final DBObject value;
		
		public AsyncWriter(DBObject id, DBObject value){
			this.id = id;
			this.value = value;
		}
		
		@Override
		public void run() {
			DBCollection dbUsers = db.getCollection(usersCollection);
			
			final DBObject dbSetUpdate = new BasicDBObject();
			dbSetUpdate.put("$set", value);

			WriteResult result = dbUsers.update(id, dbSetUpdate);
			
		}
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		writeExecutor = Executors.newFixedThreadPool(getThreadsNumber());
		
	}
}
