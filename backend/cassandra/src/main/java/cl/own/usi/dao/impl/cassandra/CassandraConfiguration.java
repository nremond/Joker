package cl.own.usi.dao.impl.cassandra;


import java.util.ArrayList;
import java.util.List;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.ColumnDef;
import org.apache.cassandra.thrift.IndexType;
import org.apache.cassandra.thrift.KsDef;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CassandraConfiguration implements InitializingBean{
	
	final static private String dbcluster = "JokerCluster";
	final static private String dbhost = "localhost";
	final static private int dbPort = 9160;
	final static private String dbKeyspace = "JokerKeySpace";
	final static public String userColumnFamily = "Users";
	
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
	        CfDef columnfamily_definition = new CfDef();
	        columnfamily_definition.setColumn_type("Standard");
	        columnfamily_definition.setName(userColumnFamily);
	        columnfamily_definition.setKeyspace(dbKeyspace);
	        
	        //Definition of the index on column Email
			ColumnDef cd1 = new ColumnDef();
        	cd1.setName("email".getBytes());
        	cd1.setIndex_name("EmailIndex");
        	cd1.setIndex_type(IndexType.KEYS);
        	cd1.setValidation_class("BytesType");
        	columnfamily_definition.addToColumn_metadata(cd1);
        	
        	//Add the column family to the keyspace
	        List<CfDef> columnfamily_list = new ArrayList<CfDef>();
			columnfamily_list.add(columnfamily_definition);
			keyspace_definition.setCf_defs(columnfamily_list);
	        
	        
	        client.system_add_keyspace(keyspace_definition);
		} finally {
			if (transport != null) {
				transport.close();
			}
		}
		
	}

}
