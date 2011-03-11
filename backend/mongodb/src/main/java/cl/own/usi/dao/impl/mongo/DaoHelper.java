package cl.own.usi.dao.impl.mongo;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.base64.Base64Dialect;
import org.jboss.netty.util.CharsetUtil;

import cl.own.usi.model.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class DaoHelper {

	public static String userIdField = "userId";
	public static String emailField = "email";
	public static String passwordField = "password";
	public static String firstnameField = "firstname";
	public static String lastnameField = "lastname";
	public static String scoreField = "score";
	public static String isLoggedField = "isLogged";
	public static String answersField = "answers";
	public static String questionNumberField = "questionNumber";
	public static String answerNumberField = "answerNumber";
	public static String bonusField = "bonus";
	
	public static String usersCollection = "users";
	
	private static final String USER_ID_SALT = "123456";
	
	
	public static DBObject toDBObject(final User user) {
		DBObject dbUser = new BasicDBObject();
		dbUser.put(userIdField, DaoHelper.generateUserId(user));
		dbUser.put(emailField, user.getEmail());
		dbUser.put(passwordField, user.getPassword());
		dbUser.put(firstnameField, user.getFirstname());
		dbUser.put(lastnameField, user.getLastname());
		dbUser.put(scoreField, user.getScore());
		dbUser.put(isLoggedField, Boolean.FALSE);
		return dbUser;
	}

	public static User fromDBObject(final DBObject dbUser) {
		User user = new User();
		user.setUserId((String) dbUser.get(userIdField));
		user.setEmail((String) dbUser.get(emailField));
		user.setPassword((String) dbUser.get(passwordField));
		user.setFirstname((String) dbUser.get(firstnameField));
		user.setLastname((String) dbUser.get(lastnameField));
		user.setScore((Integer) dbUser.get(scoreField));
		return user;
	}
	

	public static String generateUserId(final User user) {
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
