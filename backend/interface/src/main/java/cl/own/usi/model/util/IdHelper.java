package cl.own.usi.model.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IdHelper {

	private static final String BASE_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	// encode method
	public static String base64Encode(byte[] bytes) {
		StringBuilder tmp = new StringBuilder();
		int i = 0;
		byte pos;
		for (i = 0; i < bytes.length - bytes.length % 3; i += 3) {
			pos = (byte) (bytes[i] >> 2 & 63);
			tmp.append(BASE_TABLE.charAt(pos));
			pos = (byte) (((bytes[i] & 3) << 4) + (bytes[i + 1] >> 4 & 15));
			tmp.append(BASE_TABLE.charAt(pos));
			pos = (byte) (((bytes[i + 1] & 15) << 2) + (bytes[i + 2] >> 6 & 3));
			tmp.append(BASE_TABLE.charAt(pos));
			pos = (byte) (bytes[i + 2] & 63);
			tmp.append(BASE_TABLE.charAt(pos));
		}
		if (bytes.length % 3 != 0) {
			if (bytes.length % 3 == 2) {
				pos = (byte) (bytes[i] >> 2 & 63);
				tmp.append(BASE_TABLE.charAt(pos));
				pos = (byte) (((bytes[i] & 3) << 4) + (bytes[i + 1] >> 4 & 15));
				tmp.append(BASE_TABLE.charAt(pos));
				pos = (byte) ((bytes[i + 1] & 15) << 2);
				tmp.append(BASE_TABLE.charAt(pos));
				// tmp.append("=");
			} else if (bytes.length % 3 == 1) {
				pos = (byte) (bytes[i] >> 2 & 63);
				tmp.append(BASE_TABLE.charAt(pos));
				pos = (byte) ((bytes[i] & 3) << 4);
				tmp.append(BASE_TABLE.charAt(pos));
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
	
}
