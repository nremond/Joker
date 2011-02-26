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

import sun.security.krb5.Asn1Exception;

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

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String USER_ID_SALT = "123456";

	private static String usersCollection = "users";

	private static DBObject userIdIndex = new BasicDBObject("userId", 1);
	private static DBObject credentialsIndex = new BasicDBObject("email", 1)
			.append("password", 1);

	private static String userIdField = "userId";
	private static String emailField = "email";
	private static String passwordField = "password";
	private static String firstnameField = "firstname";
	private static String lastnameField = "lastname";
	private static String scoreField = "score";
	private static String isLoggedField = "isLogged";
	private static String answersField = "answers";
	private static String questionNumberField = "questionNumber";
	private static String answerNumberField = "answerNumber";

	private DBObject toDBObject(final User user) {
		DBObject dbUser = new BasicDBObject();
		dbUser.put(userIdField, generateUserId(user));
		dbUser.put(emailField, user.getEmail());
		dbUser.put(passwordField, user.getPassword());
		dbUser.put(firstnameField, user.getFirstname());
		dbUser.put(lastnameField, user.getLastname());
		dbUser.put(scoreField, user.getScore());
		dbUser.put(isLoggedField, Boolean.FALSE);
		return dbUser;
	}

	private User fromDBObject(final DBObject dbUser) {
		User user = new User();
		user.setUserId((String) dbUser.get(userIdField));
		user.setEmail((String) dbUser.get(emailField));
		user.setPassword((String) dbUser.get(passwordField));
		user.setFirstname((String) dbUser.get(firstnameField));
		user.setLastname((String) dbUser.get(lastnameField));
		user.setScore((Integer) dbUser.get(scoreField));
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
		dbId.put(userIdField, userId);

		DBObject dbUser = dbUsers.findOne(dbId);
		if (dbUser != null) {
			logger.debug("fetching userId=" + userId + " and isLogged="
					+ dbUser.get(isLoggedField));

			// The index is only on the id, isLogged can't be part of the query
			return (Boolean) dbUser.get(isLoggedField) ? fromDBObject(dbUser)
					: null;
		} else {
			logger.debug("fetching userId=" + userId
					+ " is impossible, not in db");
			return null;
		}
	}

	@Override
	public String login(final String email, final String password) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbCredentials = new BasicDBObject();
		dbCredentials.put(emailField, email);
		dbCredentials.put(passwordField, password);

		DBObject dblogin = new BasicDBObject();
		dblogin.put(isLoggedField, Boolean.TRUE);

		DBObject dbUser = dbUsers.findAndModify(dbCredentials, dblogin);

		if (dbUser != null) {
			logger.debug("login sucessful for " + email + "/" + password
					+ "->userId=" + dbUser.get("userId"));

			return (String) dbUser.get("userId");
		} else {
			logger.debug("login failed for " + email + "/" + password);

			return null;
		}
	}

	@Override
	public void logout(final String userId) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		DBObject dbUser = new BasicDBObject();
		dbUser.put(userIdField, userId);

		DBObject dblogout = new BasicDBObject();
		dblogout.put(isLoggedField, Boolean.FALSE);

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
		dbUserId.put(userIdField, answer.getUserId());

		DBObject dbAnswer = new BasicDBObject();
		dbAnswer.put(answerNumberField, answer.getAnswerNumber());
		dbAnswer.put(questionNumberField, answer.getQuestionNumber());

		DBObject dbAnswers = new BasicDBObject();
		dbAnswers.put(answersField, dbAnswer);

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
		dbId.put(userIdField, userId);

		DBObject dbUser = dbUsers.findOne(dbId);
		if (dbUser != null) {

			@SuppressWarnings("unchecked")
			List<DBObject> dbAnswers = (List<DBObject>) dbUser
					.get(answersField);
			List<Answer> answers = new ArrayList<Answer>(dbAnswers.size());
			for (DBObject dbAnswer : dbAnswers) {
				Answer answer = new Answer();
				answer.setAnswerNumber((Integer) dbAnswer
						.get(answerNumberField));
				answer.setQuestionNumber((Integer) dbAnswer
						.get(questionNumberField));
				answer.setUserId(userId);
				answers.add(answer);
			}

			logger.debug("fetching answers for userId=" + userId + "  "
					+ answers.toString());
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
