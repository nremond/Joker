package cl.own.usi.dao.impl.mongo;

import static cl.own.usi.dao.impl.mongo.DaoHelper.bonusField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.namesEmailField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.orderByScoreNames;
import static cl.own.usi.dao.impl.mongo.DaoHelper.scoreField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.userIdField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.usersCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import cl.own.usi.dao.ScoreDAO;
import cl.own.usi.model.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Repository
public class ScoreDAOMongoImpl implements ScoreDAO {

	@Autowired
	private DB db;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ScoreDAOMongoImpl.class);

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

	private final static DBObject scoreFieldsToFetch = new BasicDBObject()
			.append(scoreField, 1);

	private final static DBObject scoreBonusFieldsToFetch = new BasicDBObject()
			.append(scoreField, 1).append(bonusField, 1);

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
		// TODO NIRE set the fields we want, take everything for now
		DBObject query = new BasicDBObject();
		return getUsers(query, limit);
	}

	private DBObject generateOrderQuery(OrderOperator orderOp, User user) {
		// eg with gt :
		// (score > current_score)
		// || (score == current_score && names > current_names)

		// 1st criteria
		DBObject criteria1 = new BasicDBObject();
		DBObject scoreGT = new BasicDBObject();
		scoreGT.put(orderOp.toString(), user.getScore());
		criteria1.put(scoreField, scoreGT);

		// 2nd criteria
		DBObject criteria2 = new BasicDBObject();
		criteria2.put(scoreField, user.getScore());

		DBObject namesGT = new BasicDBObject();
		namesGT.put(orderOp.toString(), DaoHelper.getNames(user));
		criteria2.put(namesEmailField, namesGT);

		// Full query
		DBObject query = new BasicDBObject();
		query.put("$or", Arrays.asList(criteria1, criteria2));

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
	public int setBadAnswer(final String userId, final int questionNumber, final int questionValue) {

		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbUser = new BasicDBObject();
		dbUser.put(userIdField, userId);

		DBObject dbBonus = new BasicDBObject();
		dbBonus.put(bonusField, 0);
		DBObject dbSetBonus = new BasicDBObject();
		dbSetBonus.put("$set", dbBonus);

		// Only fetch the score and set the bonus to zero
		final FindAndModifyAction findAndModifyAction = new FindAndModifyAction(
				dbUsers, dbUser, scoreFieldsToFetch, null, false, dbSetBonus,
				false, false);

		DBObject user = findAndModifyAction.safeAction();

		Integer score = (Integer) user.get(scoreField);

		LOGGER.debug("setBadAnswer for user {} whose score is now {}", userId,
				score);

		return score.intValue();
	}

	@Override
	public int setGoodAnswer(final String userId, final int questionNumber,
			final int questionValue) {

		DBCollection dbUsers = db.getCollection(usersCollection);

		// Get the current score and bonus
		DBObject dbId = new BasicDBObject();
		dbId.put(userIdField, userId);
		DBObject dbUser = dbUsers.findOne(dbId, scoreBonusFieldsToFetch);

		int bonus = (Integer) dbUser.get(bonusField);
		int score = (Integer) dbUser.get(scoreField);

		// TODO Et si un user repond a 3 questions correctes d'affilees,
		// mais loupe le temps de reponse pour la 4eme, la 5eme reponse
		// si elle est correct ne doit pas profiter des 3 questions
		// precedentes enregirstrees.

		int newScore = score + bonus + questionValue;
		int newBonus = bonus + 1;

		DBObject dbScoreAndBonus = new BasicDBObject();

		dbScoreAndBonus.put(scoreField, Integer.valueOf(newScore));
		dbScoreAndBonus.put(bonusField, Integer.valueOf(newBonus));

		DBObject dbSetScoreAndBonus = new BasicDBObject();
		dbSetScoreAndBonus.put("$set", dbScoreAndBonus);

		dbUsers.update(dbId, dbSetScoreAndBonus);

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
	public void computeRankings() {
		// not needed in this implementation
	}

}