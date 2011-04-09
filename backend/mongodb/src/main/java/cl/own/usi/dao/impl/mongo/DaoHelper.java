package cl.own.usi.dao.impl.mongo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.base64.Base64Dialect;
import org.jboss.netty.util.CharsetUtil;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;
import cl.own.usi.model.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class DaoHelper {

	public static final String userIdField = "userId";
	public static final String emailField = "email";
	public static final String passwordField = "password";
	public static final String firstnameField = "firstname";
	public static final String lastnameField = "lastname";
	public static final String scoreField = "score";
	public static final String isLoggedField = "isLogged";
	public static final String answersField = "answers";
	public static final String questionNumberField = "questionNumber";
	public static final String answerNumberField = "answerNumber";
	public static final String bonusField = "bonus";

	public static final String usersCollection = "users";

	private static final String USER_ID_SALT = "[B@190d11";

	// Spec : les classements sont ordonnes par lastname/firstname/mail
	public final static DBObject orderByScoreAndNames = new BasicDBObject()
			.append(scoreField, -1).append(lastnameField, 1)
			.append(firstnameField, 1).append(emailField, 1);

	public final static DBObject orderByNames = new BasicDBObject()
			.append(lastnameField, 1).append(firstnameField, 1)
			.append(emailField, 1);

	public static DBObject toDBObject(final User user) {
		DBObject dbUser = new BasicDBObject();
		dbUser.put(userIdField, DaoHelper.generateUserId(user.getEmail()));
		dbUser.put(emailField, user.getEmail());
		dbUser.put(passwordField, user.getPassword());
		dbUser.put(firstnameField, user.getFirstname());
		dbUser.put(lastnameField, user.getLastname());
		dbUser.put(scoreField, user.getScore());
		dbUser.put(isLoggedField, Boolean.FALSE);
		dbUser.put(bonusField, Integer.valueOf(0));
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

	public static String generateUserId(final String email) {
		String hash = sha1(email + USER_ID_SALT);
		ChannelBuffer chanBuff = wrappedBuffer(hash.getBytes(CharsetUtil.UTF_8));
		return Base64.encode(chanBuff, Base64Dialect.STANDARD).toString(
				CharsetUtil.UTF_8);
	}

	private static String sha1(final String s) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		md.update((s + USER_ID_SALT).getBytes());
		return new String(md.digest());
	}

}
