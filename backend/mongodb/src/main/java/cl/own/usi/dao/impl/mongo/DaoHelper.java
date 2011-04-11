package cl.own.usi.dao.impl.mongo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

	private static final String USER_ID_SALT = "[B@190d11+";

	// Spec : les classements sont ordonnes par lastname/firstname/mail

	// TODO see if it brings something
	// public final static DBObject orderByNames = new BasicDBObject()
	// .append(lastnameField, 1).append(firstnameField, 1)
	// .append(emailField, 1);

	public final static DBObject orderByScore = new BasicDBObject().append(
			scoreField, -1);

	public final static DBObject orderByScoreLastname = new BasicDBObject()
			.append(scoreField, -1).append(lastnameField, 1);

	public final static DBObject orderByScoreLastnameFirstname = new BasicDBObject()
			.append(scoreField, -1).append(lastnameField, 1)
			.append(firstnameField, 1);

	public final static DBObject orderByScoreLastnameFirstnameEmail = new BasicDBObject()
			.append(scoreField, -1).append(lastnameField, 1)
			.append(firstnameField, 1).append(emailField, 1);

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

	static final String baseTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	// encode method
	public static String base64Encode(byte[] bytes) {
		StringBuilder tmp = new StringBuilder();
		int i = 0;
		byte pos;
		for (i = 0; i < bytes.length - bytes.length % 3; i += 3) {
			pos = (byte) (bytes[i] >> 2 & 63);
			tmp.append(baseTable.charAt(pos));
			pos = (byte) (((bytes[i] & 3) << 4) + (bytes[i + 1] >> 4 & 15));
			tmp.append(baseTable.charAt(pos));
			pos = (byte) (((bytes[i + 1] & 15) << 2) + (bytes[i + 2] >> 6 & 3));
			tmp.append(baseTable.charAt(pos));
			pos = (byte) (bytes[i + 2] & 63);
			tmp.append(baseTable.charAt(pos));
		}
		if (bytes.length % 3 != 0) {
			if (bytes.length % 3 == 2) {
				pos = (byte) (bytes[i] >> 2 & 63);
				tmp.append(baseTable.charAt(pos));
				pos = (byte) (((bytes[i] & 3) << 4) + (bytes[i + 1] >> 4 & 15));
				tmp.append(baseTable.charAt(pos));
				pos = (byte) ((bytes[i + 1] & 15) << 2);
				tmp.append(baseTable.charAt(pos));
				// tmp.append("=");
			} else if (bytes.length % 3 == 1) {
				pos = (byte) (bytes[i] >> 2 & 63);
				tmp.append(baseTable.charAt(pos));
				pos = (byte) ((bytes[i] & 3) << 4);
				tmp.append(baseTable.charAt(pos));
				// tmp.append("==");
			}
		}
		return tmp.toString();
	}

	public static String generateUserId(final String email) {
		byte[] hash = sha1(email + USER_ID_SALT);
		// return Base64.encode(hash);
		return base64Encode(hash);
	}

	private static byte[] sha1(final String s) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		md.update((s + USER_ID_SALT).getBytes());
		return md.digest();
	}

	/*
	 * I'm using this to test the userId public static void main(String[] args)
	 * { System.out.println(generateUserId("brown.king@hotmail.com"));
	 * System.out.println(generateUserId("brown.hartman@yahoo.com"));
	 * System.out.println(generateUserId("brown.gardner@gmail.com"));
	 * System.out.println(generateUserId("brown.mcfarland@gmail.com"));
	 * System.out.println(generateUserId("brown.mckay@gmail.com"));
	 * System.out.println(generateUserId("brown.hurst@gmail.com")); }
	 */

}
