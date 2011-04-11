package cl.own.usi.dao.impl.mongo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cl.own.usi.model.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class DaoHelper {

	public static final String userIdField = "userId";
	public static final String passwordField = "password";
	public static final String scoreField = "score";
	public static final String isLoggedField = "isLogged";
	public static final String answersField = "answers";
	public static final String questionNumberField = "questionNumber";
	public static final String answerNumberField = "answerNumber";
	public static final String bonusField = "bonus";
	public static final String namesEmailField = "namesEmail";

	public static final String usersCollection = "users";

	private static final String namesEmailSeparator = "!!!";

	public final static DBObject orderByScore = new BasicDBObject().append(
			scoreField, -1);

	public final static DBObject orderByNames = new BasicDBObject().append(
			namesEmailField, -1);

	public final static DBObject orderByScoreNames = new BasicDBObject()
			.append(scoreField, -1).append(namesEmailField, 1);

	public static DBObject toDBObject(final User user) {
		DBObject dbUser = new BasicDBObject();
		dbUser.put(userIdField, DaoHelper.generateUserId(user.getEmail()));
		dbUser.put(passwordField, user.getPassword());
		dbUser.put(scoreField, user.getScore());
		dbUser.put(isLoggedField, Boolean.FALSE);
		dbUser.put(bonusField, Integer.valueOf(0));

		// Special field used for ranking
		dbUser.put(namesEmailField, getNames(user));

		return dbUser;
	}

	public static String getNames(User user) {

		// Spec : les classements sont ordonnes par lastname/firstname/mail
		StringBuilder sb = new StringBuilder();
		sb.append(user.getLastname());
		sb.append(namesEmailSeparator);
		sb.append(user.getFirstname());
		sb.append(namesEmailSeparator);
		sb.append(user.getEmail());
		return sb.toString();
	}

	public static User fromDBObject(final DBObject dbUser) {
		User user = new User();
		user.setUserId((String) dbUser.get(userIdField));
		user.setPassword((String) dbUser.get(passwordField));
		user.setScore((Integer) dbUser.get(scoreField));

		String namesEmail = (String) dbUser.get(namesEmailField);
		String[] details = namesEmail.split(namesEmailSeparator);

		if (details.length != 3) {
			throw new RuntimeException(
					"Invalid namesEmail field, the DB is in bad shape");
		}

		String lastname = details[0];
		String firstname = details[1];
		String email = details[2];

		user.setEmail((String) email);
		user.setFirstname((String) firstname);
		user.setLastname((String) lastname);

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

	private static final String USER_ID_SALT = "[B@190d11+";

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
