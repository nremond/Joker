package cl.own.usi.dao.impl.cassandra;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.base64.Base64Dialect;
import org.jboss.netty.util.CharsetUtil;

import cl.own.usi.model.User;

public class CassandraHelper {

	private static final String USER_ID_SALT = "123456";
	
	public static String generateUserId(final String email) {
		ChannelBuffer chanBuff = wrappedBuffer((email + USER_ID_SALT)
				.getBytes(CharsetUtil.UTF_8));
		return Base64.encode(chanBuff, Base64Dialect.ORDERED).toString(
				CharsetUtil.UTF_8);
	}
	public static String generateUserId(final User user) {
		return generateUserId(user.getEmail());
	}
	
}
