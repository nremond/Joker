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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CassandraConfiguration implements InitializingBean {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// Cluster
	final static private String dbcluster = "JokerCluster";
	final static private String dbhost = "localhost";
	final static private int dbPort = 9160;

	// Keyspace
	final static public String dbKeyspace = "JokerKeySpace";

	// Column Family
	final static public String usersColumnFamily = "UsersInfo";
	final static public String answersColumnFamily = "UsersAnswers";
	final static public String bonusesColumnFamily = "UsersBonuses";
	final static public String ranksColumnFamily = "Ranks";
	final static public String loginsColumnFamily = "UsersLogins";
	final static public String scoresColumnFamily = "UsersScores";

	// Column name
	final static String emailColumn = "email";
	final static String passwordColumn = "password";
	final static String firstnameColumn = "firstname";
	final static String lastnameColumn = "lastname";
	
	private Cluster cluster;
	
	private int replicationFactor = 1; // 2; // 1;
	private String strategyClass = "LocalStrategy"; //"LocalStrategy"; // "SimpleStrategy"
	private boolean forceKeyspaceRecreation = true;

	@Value(value = "${backend.cassandra.replicationFactor:1}")
	public void setReplicationFactor(int replicationFactor) {
		this.replicationFactor = replicationFactor;
	}
	
	@Value(value = "${backend.cassandra.replicationStrategyClass:LocalStrategy}")
	public void setReplicationStrategyClass(String  strategyClass) {
		this.strategyClass = strategyClass;
	}
	
	@Value(value = "${backend.cassandra.forceKeyspaceRecreation:true}")
	public void setForceKeyspaceRecreation(boolean forceKeyspaceRecreation) {
		this.forceKeyspaceRecreation = forceKeyspaceRecreation;
	}
	
	@Bean
	public Cluster cluster() {
		return HFactory.getOrCreateCluster(dbcluster, dbhost + ":" + dbPort);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		cluster = cluster();
		
		boolean keyspaceExists = keyspaceExists();

		if (!keyspaceExists || forceKeyspaceRecreation) {
			
			if (keyspaceExists) {
				dropKeyspace();
			}

			createKeyspace();

		}
		
	}
	

	public void dropKeyspace() {
		
		logger.debug("Droping keyspace");
		
		cluster.dropKeyspace(dbKeyspace);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {}
		
	}

	public void createKeyspace() {
		
		
		logger.debug("Creating keyspace");
		
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
		usersColumnFamilyDefinition.setKeyCacheSize(1000000);
		usersColumnFamilyDefinition.setRowCacheSize(1000000);
//		usersColumnFamilyDefinition.setReadRepairChance(0);
		
		keyspaceDefinition.addColumnFamily(new ThriftCfDef(
				usersColumnFamilyDefinition));
		

		BasicColumnFamilyDefinition answersColumnFamilyDefinition = new BasicColumnFamilyDefinition();
		answersColumnFamilyDefinition
				.setComparatorType(ComparatorType.INTEGERTYPE);
		answersColumnFamilyDefinition.setName(answersColumnFamily);
		answersColumnFamilyDefinition.setKeyspaceName(dbKeyspace);
		answersColumnFamilyDefinition.setKeyCacheSize(1000000);
		keyspaceDefinition.addColumnFamily(new ThriftCfDef(
				answersColumnFamilyDefinition));

		BasicColumnFamilyDefinition bonusesColumnFamilyDefinition = new BasicColumnFamilyDefinition();
		bonusesColumnFamilyDefinition.setComparatorType(ComparatorType.INTEGERTYPE);
		bonusesColumnFamilyDefinition.setName(bonusesColumnFamily);
		bonusesColumnFamilyDefinition.setKeyspaceName(dbKeyspace);
		bonusesColumnFamilyDefinition.setKeyCacheSize(1000000);
		bonusesColumnFamilyDefinition.setRowCacheSize(1000000);
		keyspaceDefinition.addColumnFamily(new ThriftCfDef(
				bonusesColumnFamilyDefinition));
		
		BasicColumnFamilyDefinition ranksColumnFamilyDefinition = new BasicColumnFamilyDefinition();
		ranksColumnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
		ranksColumnFamilyDefinition.setName(ranksColumnFamily);
		ranksColumnFamilyDefinition.setKeyspaceName(dbKeyspace);
		ranksColumnFamilyDefinition.setKeyCacheSize(1000);
		ranksColumnFamilyDefinition.setRowCacheSize(1000);
		keyspaceDefinition.addColumnFamily(new ThriftCfDef(
				ranksColumnFamilyDefinition));

		BasicColumnFamilyDefinition loginsColumnFamilyDefinition = new BasicColumnFamilyDefinition();
		loginsColumnFamilyDefinition.setComparatorType(ComparatorType.LONGTYPE);
		loginsColumnFamilyDefinition.setName(loginsColumnFamily);
		loginsColumnFamilyDefinition.setKeyspaceName(dbKeyspace);
		loginsColumnFamilyDefinition.setKeyCacheSize(1000000);
		loginsColumnFamilyDefinition.setRowCacheSize(1000000);
		keyspaceDefinition.addColumnFamily(new ThriftCfDef(
				loginsColumnFamilyDefinition));
		
		BasicColumnFamilyDefinition scoresColumnFamilyDefinition = new BasicColumnFamilyDefinition();
		scoresColumnFamilyDefinition
				.setComparatorType(ComparatorType.INTEGERTYPE);
		scoresColumnFamilyDefinition.setName(scoresColumnFamily);
		scoresColumnFamilyDefinition.setKeyspaceName(dbKeyspace);
		scoresColumnFamilyDefinition.setKeyCacheSize(1000000);
		scoresColumnFamilyDefinition.setRowCacheSize(1000000);
		keyspaceDefinition.addColumnFamily(new ThriftCfDef(
				scoresColumnFamilyDefinition));
		
		cluster.addKeyspace(new ThriftKsDef(keyspaceDefinition));
		
	}

	public boolean keyspaceExists() {
		boolean keyspaceExists = false;
		List<KeyspaceDefinition> keyspaces = cluster.describeKeyspaces();
		for (KeyspaceDefinition kd : keyspaces) {
			if (kd.getName().equals(dbKeyspace)) {
				keyspaceExists = true;
				break;
			}
		}
		return keyspaceExists;
	}

}
