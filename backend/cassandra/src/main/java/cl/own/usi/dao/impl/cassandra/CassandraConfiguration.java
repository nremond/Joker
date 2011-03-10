package cl.own.usi.dao.impl.cassandra;


import java.util.ArrayList;
import java.util.List;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.KsDef;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CassandraConfiguration implements InitializingBean{
	
	//Cluster
	final static private String dbcluster = "JokerCluster";
	final static private String dbhost = "localhost";
	final static private int dbPort = 9160;
	
	//Keyspace
	final static private String dbKeyspace = "JokerKeySpace";
	
	//Column Family
	final static public String usersColumnFamily = "Users";
	final static public String answersColumnFamily = "Answers";
	final static public String emailsColumnFamily = "Emails";
	final static public String requestsColumnFamily = "Requests";
	
	//Column name
	final static String userIdColumn = "userId";
	final static String emailColumn = "email";
	final static String passwordColumn = "password";
	final static String firstnameColumn = "firstname";
	final static String lastnameColumn = "lastname";
	final static String scoreColumn = "score";
	final static String isLoggedColumn = "isLogged";
	final static String answersColumn = "answers";
	final static String questionNumberColumn = "questionNumber";
	final static String answerNumberColumn = "answerNumber";
	
	
	
	@Bean
	public Cluster cluster(){
		return HFactory.getOrCreateCluster(dbcluster, dbhost+":"+dbPort);
	}
	
	@Bean
	public Keyspace keyspace(){
		return HFactory.createKeyspace(dbKeyspace, cluster());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		TFramedTransport transport = null;
		try {
			transport = new TFramedTransport(new TSocket(dbhost, dbPort));
			Cassandra.Client client = new Cassandra.Client(new TBinaryProtocol(transport));
			transport.open();
			
			List<KsDef> keyspaceList = client.describe_keyspaces();
			for(KsDef keyspace:keyspaceList){
				if (keyspace.getName().equals(dbKeyspace)){
					client.send_system_drop_keyspace(dbKeyspace);
					client.recv_system_drop_keyspace();
				}
			}
	
			//Definition of the Keyspace
			KsDef keyspace_definition = new KsDef();
			keyspace_definition.setReplication_factor(1);
			keyspace_definition.setStrategy_class("LocalStrategy");
			keyspace_definition.setName(dbKeyspace);
			
			//Definition of the column family Users
	        CfDef users_CfDef = new CfDef();
	        users_CfDef.setColumn_type("Standard");
	        users_CfDef.setName(usersColumnFamily);
	        users_CfDef.setKeyspace(dbKeyspace);
	        
	        //Definition of the column family Email
	        CfDef emails_CfDef = new CfDef();
	        emails_CfDef.setColumn_type("Standard");
	        emails_CfDef.setName(emailsColumnFamily);
	        emails_CfDef.setKeyspace(dbKeyspace);
	        
	        //Definition of the column family Requests
	        /*CfDef request_CfDef = new CfDef();
	        request_CfDef.setColumn_type("Standard");
	        request_CfDef.setName(requestsColumnFamily);
	        request_CfDef.setKeyspace(dbKeyspace);*/
	        
	        //Definition of the column family Answers
	        CfDef answers_CfDef = new CfDef();
	        answers_CfDef.setColumn_type("Super");
	        answers_CfDef.setName(answersColumnFamily);
	        answers_CfDef.setKeyspace(dbKeyspace);
	        
	        //Definition of the index on column Email
			/*ColumnDef email_CDef = new ColumnDef();
			email_CDef.setName("email".getBytes());
			email_CDef.setIndex_name("EmailIndex");
			email_CDef.setIndex_type(IndexType.KEYS);
			email_CDef.setValidation_class("BytesType");
        	users_CfDef.addToColumn_metadata(email_CDef);*/
        	
        	
        	
   
        	//Add the column family to the keyspace
	        List<CfDef> columnfamily_list = new ArrayList<CfDef>();
			columnfamily_list.add(users_CfDef);
			columnfamily_list.add(emails_CfDef);
			columnfamily_list.add(answers_CfDef);
			keyspace_definition.setCf_defs(columnfamily_list);
	                
	        client.system_add_keyspace(keyspace_definition);
		} finally {
			if (transport != null) {
				transport.close();
			}
		}
		
	}

}
