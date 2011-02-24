package cl.own.usi.dao.impl.mongo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import cl.own.usi.dao.UserDAO;
import cl.own.usi.model.Answer;
import cl.own.usi.model.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

@Repository
public class UserDAOMongoImpl implements UserDAO {

	@Autowired
	DB db;

	private static String usersCollection = "users";
	private static String answersCollection = "answers";

	private static DBObject emailIndex = new BasicDBObject("email", 1);
	private static DBObject credentialsIndex = new BasicDBObject("email", 1)
			.append("password", 1);

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private DBObject toDBObject(User user) {
		DBObject dbUser = new BasicDBObject();
		dbUser.put("email", user.getEmail());
		dbUser.put("password", user.getPassword());
		dbUser.put("firstname", user.getFirstname());
		dbUser.put("lastname", user.getLastname());
		dbUser.put("score", user.getScore());
		dbUser.put("isLogged", false);
		return dbUser;
	}

	private User fromDBObject(DBObject dbUser) {
		User user = new User();
		user.setEmail((String) dbUser.get("email"));
		user.setPassword((String) dbUser.get("password"));
		user.setFirstname((String) dbUser.get("firstname"));
		user.setLastname((String) dbUser.get("lastname"));
		user.setScore((Integer) dbUser.get("score"));
		return user;
	}

	@Override
	public boolean insertUser(User user) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		// the driver keeps a cache of the added index
		dbUsers.ensureIndex(emailIndex, "emailIndex", true);
		dbUsers.ensureIndex(credentialsIndex, "credentialsIndex", false);

		DBObject dbUser = toDBObject(user);

		WriteResult wr = dbUsers.insert(dbUser);
		String error = wr.getError();

		// E11000 -> duplicate key
		if (StringUtils.hasText(error) && error.indexOf("E11000") == 0) {
			logger.debug("user " + user.getEmail()
					+ " was already in the collection, insertion aborted");
			return false;
		} else {
			logger.debug("user " + user.getEmail()
					+ " was successfully inserted");
			return true;
		}
	}

	@Override
	public User getUserById(String userId) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbId = new BasicDBObject();
		dbId.put("_id", userId);

		DBObject dbUser = dbUsers.findOne(dbId);
		if (dbUser != null) {
			// I don't put in the query because the index is only on the _id
			// field
			return (Boolean) dbUser.get("isLogged") ? fromDBObject(dbUser)
					: null;
		} else {
			return null;
		}
	}

	@Override
	public String login(String email, String password) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbCredentials = new BasicDBObject();
		dbCredentials.put("email", email);
		dbCredentials.put("password", password);

		DBObject dblogin = new BasicDBObject();
		dblogin.put("isLogged", true);

		DBObject dbUser = dbUsers.findAndModify(dbCredentials, dblogin);

		if (dbUser != null) {
			return (String) dbUser.get("_id");
		} else {
			return null;
		}
	}

	@Override
	public void logout(String userId) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbUser = new BasicDBObject();
		dbUser.put("_id", userId);

		DBObject dblogout = new BasicDBObject();
		dblogout.put("isLogged", false);

		dbUsers.findAndModify(dbUser, dblogout);
	}

	@Override
	public void insertRequest(String userId, int questionNumber) {

		DBCollection dbAnswers = db.getCollection(answersCollection);

		// the driver keeps a cache of the added index
		// dbAnswers.ensureIndex(credentialsIndex, "emailIndex", true);

		DBObject dbAnswer = new BasicDBObject();

		// TODO finish
	}

	@Override
	public void insertAnswer(Answer answer) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Answer> getAnswers(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flushUsers() {
		// TODO Auto-generated method stub

	}

}
