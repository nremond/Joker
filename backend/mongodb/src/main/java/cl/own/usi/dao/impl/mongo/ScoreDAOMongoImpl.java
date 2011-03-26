package cl.own.usi.dao.impl.mongo;

import static cl.own.usi.dao.impl.mongo.DaoHelper.bonusField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.emailField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.firstnameField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.lastnameField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.scoreField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.userIdField;
import static cl.own.usi.dao.impl.mongo.DaoHelper.usersCollection;

import java.util.ArrayList;
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
	DB db;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// Spec : les classements sont ordonnes par lastname/firstname/mail
	private static DBObject orderBy = new BasicDBObject()
			.append(scoreField, -1).append(lastnameField, 1)
			.append(firstnameField, 1).append(emailField, 1);

	private List<User> getUsers(DBObject query, DBObject querySubset,
			int expectedSize) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		DBCursor dbCursor = dbUsers.find(query, querySubset).sort(orderBy);

		List<User> users = new ArrayList<User>();
		while (dbCursor.hasNext()) {
			DBObject dbUser = dbCursor.next();
			User user = DaoHelper.fromDBObject(dbUser);
			users.add(user);
		}
		return users;
	}

	@Override
	public List<User> getTop(int limit) {
		// TODO set the fields we want, take everything for now
		DBObject query = new BasicDBObject();

		DBObject subset = new BasicDBObject();
		subset.put("$slice", limit);
		DBObject querySubset = new BasicDBObject();
		querySubset.put("comment", subset);

		List<User> users = getUsers(query, querySubset, limit);

		logger.debug("get the {} top scores : {}", limit, users);

		return users;
	}

	@Override
	public List<User> getBefore(User user, int limit) {
		DBObject criteria = new BasicDBObject();
		criteria.put("$gt", user.getScore());
		DBObject query = new BasicDBObject();
		query.put(scoreField, criteria);

		DBObject subset = new BasicDBObject();
		subset.put("$slice", -limit);
		DBObject querySubset = new BasicDBObject();
		querySubset.put("comment", subset);

		List<User> users = getUsers(query, querySubset, limit);

		logger.debug("get the {} users before {} : {} ", new Object[] { limit,
				user.getUserId(), users });

		return users;
	}

	@Override
	public List<User> getAfter(User user, int limit) {
		DBObject criteria = new BasicDBObject();
		criteria.put("$lt", user.getScore());
		DBObject query = new BasicDBObject();
		query.put(scoreField, criteria);

		DBObject subset = new BasicDBObject();
		subset.put("$slice", limit);
		DBObject querySubset = new BasicDBObject();
		querySubset.put("comment", subset);

		List<User> users = getUsers(query, querySubset, limit);

		logger.debug("get the {} users after {} : {} ", new Object[] { limit,
				user.getUserId(), users });

		return users;
	}

	@Override
	public int setBadAnswer(String userId, int questionNumber) {

		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbUser = new BasicDBObject();
		dbUser.put(userIdField, userId);

		DBObject dbBonus = new BasicDBObject();
		dbBonus.put(bonusField, 0);
		DBObject dbSetBonus = new BasicDBObject();
		dbSetBonus.put("$set", dbBonus);

		DBObject user = dbUsers.findAndModify(dbUser, dbSetBonus);
		Integer score = (Integer) user.get(scoreField);

		logger.debug("setBadAnswer for user {} whose score is now {}", userId,
				score);

		return score.intValue();
	}

	@Override
	public int setGoodAnswer(String userId, int questionNumber, int questionValue) {
		// TODO this implement is not good, we have to do this in one shot

		DBCollection dbUsers = db.getCollection(usersCollection);

		// Get the current score and bonus
		DBObject dbId = new BasicDBObject();
		dbId.put(userIdField, userId);
		DBObject dbUser = dbUsers.findOne(dbId);

		int bonus = (Integer) dbUser.get(bonusField);
		int score = (Integer) dbUser.get(scoreField);

		DBObject dbScoreAndBonus = new BasicDBObject();
		dbScoreAndBonus.put(scoreField,
				Integer.valueOf(score + bonus + questionValue));
		dbScoreAndBonus.put(bonusField, Integer.valueOf(bonus + 1));

		DBObject dbSetScoreAndBonus = new BasicDBObject();
		dbSetScoreAndBonus.put("$set", dbScoreAndBonus);

		dbUser = dbUsers.findAndModify(dbId, dbSetScoreAndBonus);
		int newScore = (Integer) dbUser.get(scoreField);

		logger.debug(
				"setGoodAnswer for user {} whose previous score was {} and is now {}",
				new Object[] { userId, score, newScore });

		return newScore;
	}

	@Override
	public void flushUsers() {
		// TODO Auto-generated method stub
	}

}
