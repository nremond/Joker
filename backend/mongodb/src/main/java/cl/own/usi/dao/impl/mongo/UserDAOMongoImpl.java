package cl.own.usi.dao.impl.mongo;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.base64.Base64Dialect;
import org.jboss.netty.util.CharsetUtil;
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

	private static final String USER_ID_SALT = "123456";

	private static String usersCollection = "users";

	private static DBObject userIdIndex = new BasicDBObject("userId", 1);
	private static DBObject credentialsIndex = new BasicDBObject("email", 1)
			.append("password", 1);

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private DBObject toDBObject(final User user) {
		DBObject dbUser = new BasicDBObject();
		dbUser.put("userId", generateUserId(user));
		dbUser.put("email", user.getEmail());
		dbUser.put("password", user.getPassword());
		dbUser.put("firstname", user.getFirstname());
		dbUser.put("lastname", user.getLastname());
		dbUser.put("score", user.getScore());
		dbUser.put("isLogged", false);
		return dbUser;
	}

	private User fromDBObject(final DBObject dbUser) {
		User user = new User();
		user.setUserId((String) dbUser.get("userId"));
		user.setEmail((String) dbUser.get("email"));
		user.setPassword((String) dbUser.get("password"));
		user.setFirstname((String) dbUser.get("firstname"));
		user.setLastname((String) dbUser.get("lastname"));
		user.setScore((Integer) dbUser.get("score"));
		return user;
	}

	@Override
	public boolean insertUser(final User user) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		// the driver keeps a cache of the added index
		dbUsers.ensureIndex(userIdIndex, "userIdIndex", true);
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
	public User getUserById(final String userId) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbId = new BasicDBObject();
		dbId.put("userId", userId);

		DBObject dbUser = dbUsers.findOne(dbId);
		if (dbUser != null) {
			// The index is only on the id, isLogged can't be part of the query
			return (Boolean) dbUser.get("isLogged") ? fromDBObject(dbUser)
					: null;
		} else {
			return null;
		}
	}

	@Override
	public String login(final String email, final String password) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbCredentials = new BasicDBObject();
		dbCredentials.put("email", email);
		dbCredentials.put("password", password);

		DBObject dblogin = new BasicDBObject();
		dblogin.put("isLogged", true);

		DBObject dbUser = dbUsers.findAndModify(dbCredentials, dblogin);

		if (dbUser != null) {
			return (String) dbUser.get("userId");
		} else {
			return null;
		}
	}

	@Override
	public void logout(final String userId) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbUser = new BasicDBObject();
		dbUser.put("userId", userId);

		DBObject dblogout = new BasicDBObject();
		dblogout.put("isLogged", false);

		dbUsers.findAndModify(dbUser, dblogout);
	}

	@Override
	public void insertRequest(final String userId, final int questionNumber) {

		// TODO finish
	}

	@Override
	public void insertAnswer(final Answer answer) {

		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbUserId = new BasicDBObject();
		dbUserId.put("userId", answer.getUserId());

		DBObject dbAnswer = new BasicDBObject();
		dbAnswer.put("answerNumber", answer.getAnswerNumber());
		dbAnswer.put("questionNumber", answer.getQuestionNumber());

		DBObject dbAnswers = new BasicDBObject();
		dbAnswers.put("answers", dbAnswer);

		DBObject dbPushAnswers = new BasicDBObject();
		dbPushAnswers.put("$push", dbAnswers);

		dbUsers.findAndModify(dbUserId, dbPushAnswers);

		logger.debug("answer inserted, "
				+ ToStringBuilder.reflectionToString(answer));

	}

	@Override
	public List<Answer> getAnswers(final String userId) {

		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbId = new BasicDBObject();
		dbId.put("userId", "userId");

		DBObject dbUser = dbUsers.findOne(dbId);
		if (dbUser != null) {

			@SuppressWarnings("unchecked")
			List<DBObject> dbAnswers = (List<DBObject>) dbUser.get("answers");
			List<Answer> answers = new ArrayList<Answer>(dbAnswers.size());
			for (DBObject dbAnswer : dbAnswers) {
				Answer answer = new Answer();
				answer.setAnswerNumber((Integer) dbAnswer.get("answerNumber"));
				answer.setQuestionNumber((Integer) dbAnswer
						.get("questionNumber"));
				answer.setUserId(userId);
				answers.add(answer);
			}

			logger.debug(answers.toString());
			return answers;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public void flushUsers() {
		// TODO Auto-generated method stub

	}

	private String generateUserId(final User user) {
		ChannelBuffer chanBuff = wrappedBuffer((user.getEmail() + USER_ID_SALT)
				.getBytes(CharsetUtil.UTF_8));
		return Base64.encode(chanBuff, Base64Dialect.ORDERED).toString(
				CharsetUtil.UTF_8);
	}

	/*
	 * private String sha1(final String email) { MessageDigest md =
	 * MessageDigest.getInstance("SHA"); md.update(email.getBytes()); byte[]
	 * digest = md.digest(); return new String(digest); }
	 */

}
