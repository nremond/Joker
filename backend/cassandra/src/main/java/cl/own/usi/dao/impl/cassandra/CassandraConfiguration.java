package cl.own.usi.dao.impl.cassandra;

import java.util.List;

import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.model.BasicKeyspaceDefinition;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CassandraConfiguration implements InitializingBean {

	// Cluster
	final static private String dbcluster = "JokerCluster";
	final static private String dbhost = "localhost";
	final static private int dbPort = 9160;

	// Keyspace
	final static public String dbKeyspace = "JokerKeySpace";

	// Column Family
	final static public String usersColumnFamily = "Users";
	final static public String answersColumnFamily = "Answers";
	// final static public String emailsColumnFamily = "Emails";
	// final static public String requestsColumnFamily = "Requests";
	final static public String bonusesColumnFamily = "Bonuses";
	final static public String ranksColumnFamily = "Ranks";

	// Column name
	final static String userIdColumn = "userId";
	final static String emailColumn = "email";
	final static String passwordColumn = "password";
	final static String firstnameColumn = "firstname";
	final static String lastnameColumn = "lastname";
	final static String scoreColumn = "score";
	final static String bonusColumn = "bonus";
	final static String isLoggedColumn = "isLogged";
	final static String answersColumn = "answers";
	final static String questionNumberColumn = "questionNumber";
	final static String answerNumberColumn = "answerNumber";

	private int replicationFactor = 1;
	private String strategyClass = "LocalStrategy"; // "SimpleStrategy"
	private boolean forceRecreation = true;

	@Bean
	public Cluster cluster() {
		return HFactory.getOrCreateCluster(dbcluster, dbhost + ":" + dbPort);
	}

//	@Bean
//	public Keyspace keyspace() {
//		return HFactory.createKeyspace(dbKeyspace, cluster());
//	}

	@Override
	public void afterPropertiesSet() throws Exception {

		boolean keyspaceExists = keyspaceExists();

		if (!keyspaceExists || forceRecreation) {

			
			if (keyspaceExists) {
				dropKeyspace();
			}

			createKeyspace();

		}

	}

	public void dropKeyspace() {
		cluster().dropKeyspace(dbKeyspace);
	}

	public void createKeyspace() {
		// Definition of the Keyspace
		BasicKeyspaceDefinition keyspaceDefinition = new BasicKeyspaceDefinition();
		keyspaceDefinition.setName(dbKeyspace);
		keyspaceDefinition.setReplicationFactor(replicationFactor);
		keyspaceDefinition.setStrategyClass(strategyClass);
		// keyspaceDefinition.setStrategyOption(field, value);

		BasicColumnFamilyDefinition usersColumnFamilyDefinition = new BasicColumnFamilyDefinition();
		usersColumnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
		usersColumnFamilyDefinition.setName(usersColumnFamily);
		usersColumnFamilyDefinition.setKeyspaceName(dbKeyspace);
		keyspaceDefinition.addColumnFamily(new ThriftCfDef(
				usersColumnFamilyDefinition));

		BasicColumnFamilyDefinition answerColumnFamilyDefinition = new BasicColumnFamilyDefinition();
		answerColumnFamilyDefinition
				.setComparatorType(ComparatorType.INTEGERTYPE);
		answerColumnFamilyDefinition.setName(answersColumnFamily);
		answerColumnFamilyDefinition.setKeyspaceName(dbKeyspace);
		keyspaceDefinition.addColumnFamily(new ThriftCfDef(
				answerColumnFamilyDefinition));

		BasicColumnFamilyDefinition bonusesColumnFamilyDefinition = new BasicColumnFamilyDefinition();
		bonusesColumnFamilyDefinition.setComparatorType(ComparatorType.INTEGERTYPE);
		bonusesColumnFamilyDefinition.setName(bonusesColumnFamily);
		bonusesColumnFamilyDefinition.setKeyspaceName(dbKeyspace);
		keyspaceDefinition.addColumnFamily(new ThriftCfDef(
				bonusesColumnFamilyDefinition));
		
		BasicColumnFamilyDefinition ranksColumnFamilyDefinition = new BasicColumnFamilyDefinition();
		ranksColumnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
		ranksColumnFamilyDefinition.setName(ranksColumnFamily);
		ranksColumnFamilyDefinition.setKeyspaceName(dbKeyspace);
		keyspaceDefinition.addColumnFamily(new ThriftCfDef(
				ranksColumnFamilyDefinition));

		cluster().addKeyspace(new ThriftKsDef(keyspaceDefinition));
	}

	public boolean keyspaceExists() {

		boolean keyspaceExists = false;
		List<KeyspaceDefinition> keyspaces = cluster().describeKeyspaces();
		for (KeyspaceDefinition kd : keyspaces) {
			if (kd.getName().equals(dbKeyspace)) {
				keyspaceExists = true;
				break;
			}
		}

		return keyspaceExists;
	}
}
