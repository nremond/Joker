package cl.own.usi.dao.impl.mongo;

import java.net.UnknownHostException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.DB;
import com.mongodb.Mongo;

@Configuration
public class MongoConfiguration {
	final private String dbName = "joker";
	final private String dbHost = "localhost";
	final private int dbPort = 27017;

	@Bean
	public DB db() throws UnknownHostException {
		return mongo().getDB(dbName);
	}

	@Bean
	public Mongo mongo() throws UnknownHostException {
		return new Mongo(dbHost, dbPort);
	}
}
