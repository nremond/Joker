package cl.own.usi.dao.impl.cassandra;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.base64.Base64Dialect;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cl.own.usi.model.User;

public class CassandraHelper {

	private static final String USER_ID_SALT = "123456";
	
	private static Logger logger = LoggerFactory.getLogger(CassandraHelper.class);
	
	public static byte[] serialize(Object obj) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(out);
			oout.writeObject(obj);
			oout.close();
			return out.toByteArray();
		} catch (IOException e) {
			logger.error("Serialization error", e);
		}
		return null;
	}

	public static Object deserialize(final byte[] value) {
		try {
			final ByteArrayInputStream in = new ByteArrayInputStream(value);
			final ObjectInputStream oin = new ObjectInputStream(in);
			final Object retval = oin.readObject();
			oin.close();
			return retval;
		} catch (Exception e) {
			logger.error("Deserialization error", e);
		}
		return null;
	}
	
	
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
