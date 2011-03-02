package cl.own.usi.dao.impl.cassandra;

import java.util.List;

import me.prettyprint.cassandra.model.IndexedSlicesQuery;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import cl.own.usi.dao.UserDAO;
import cl.own.usi.model.Answer;
import cl.own.usi.model.User;

@Repository
public class UserDAOCassandraImpl implements UserDAO{
	
	@Autowired
	Cluster cluster;
	
	@Autowired
	Keyspace keyspace;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	final static StringSerializer ss = StringSerializer.get();
	final static BytesArraySerializer bs = BytesArraySerializer.get();
	
	public static String userIdField = "userId";
	public static String emailColumn = "email";
	public static String passwordColumn = "password";
	public static String firstnameColumn = "firstname";
	public static String lastnameColumn = "lastname";
	public static String scoreColumn = "score";
	public static String isLoggedColumn = "isLogged";
	public static String answersColumn = "answers";
	public static String questionNumberColumn = "questionNumber";
	public static String answerNumberColumn = "answerNumber";
	
	public static String userColumnFamily = CassandraConfiguration.userColumnFamily;
	

	@Override
	public boolean insertUser(User user) {
		try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
          
            String userID = CassandraHelper.generateUserId(user);
            
            mutator.addInsertion(userID, userColumnFamily, HFactory.createColumn(emailColumn, user.getEmail().getBytes(), ss, bs));
            mutator.addInsertion(userID, userColumnFamily, HFactory.createColumn(firstnameColumn, user.getFirstname().getBytes(), ss, bs));
			mutator.addInsertion(userID, userColumnFamily, HFactory.createColumn(lastnameColumn, user.getLastname().getBytes(), ss, bs));
			mutator.addInsertion(userID, userColumnFamily, HFactory.createColumn(passwordColumn, user.getPassword().getBytes(), ss, bs));
			mutator.addInsertion(userID, userColumnFamily, HFactory.createColumn(scoreColumn, CassandraHelper.serialize(new Integer(user.getScore())), ss, bs));
			mutator.addInsertion(userID, userColumnFamily, HFactory.createColumn(isLoggedColumn, CassandraHelper.serialize(Boolean.FALSE), ss, bs));
			
            mutator.execute();  
            logger.debug("user " + user.getEmail()
					+ " was successfully inserted");
        } catch (HectorException e) {
            e.printStackTrace();
            logger.error("An error occured while inserting user"+e);
            return false;
        }
		return true;
	}

	@Override
	public User getUserById(String userId) {	
		SliceQuery<String, String, byte[]> q = HFactory.createSliceQuery(keyspace, ss, ss, bs);
		
		q.setColumnFamily(userColumnFamily);
		q.setKey(userId);
		q.setColumnNames(emailColumn,firstnameColumn,lastnameColumn,passwordColumn,scoreColumn);
		
		QueryResult<ColumnSlice<String, byte[]>> result = q.execute();
		ColumnSlice<String,byte[]> cs = result.get();
		
		if(cs.getColumns().size() != 0){
			User user = new User();
			user.setEmail(new String(cs.getColumnByName(emailColumn).getValue()));
			user.setFirstname(new String(cs.getColumnByName(firstnameColumn).getValue()));
			user.setLastname(new String(cs.getColumnByName(lastnameColumn).getValue()));
			user.setPassword(new String(cs.getColumnByName(passwordColumn).getValue()));
			user.setScore((Integer)CassandraHelper.deserialize(cs.getColumnByName(scoreColumn).getValue()));
			user.setUserId(userId);
			return user;
		}
		else{
			logger.debug("fetching userId=" + userId
					+ " is impossible, not in db");
			return null;
		}
	}

	@Override
	public void insertRequest(String userId, int questionNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertAnswer(Answer answer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Answer> getAnswers(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String login(String email, String password) {	
		IndexedSlicesQuery<String, String, byte[]> indexedSlicesQuery = 
            HFactory.createIndexedSlicesQuery(keyspace, ss, ss, bs);
		
        indexedSlicesQuery.addEqualsExpression(emailColumn, email.getBytes());
        indexedSlicesQuery.addEqualsExpression(passwordColumn, password.getBytes());
        indexedSlicesQuery.setColumnNames(emailColumn,passwordColumn);
        indexedSlicesQuery.setColumnFamily(userColumnFamily);
        indexedSlicesQuery.setStartKey("");
        
        QueryResult<OrderedRows<String, String, byte[]>> result = indexedSlicesQuery.execute();
        OrderedRows<String, String, byte[]> or = result.get();
        
        if (or.getCount() == 1){
        	Row<String, String, byte[]> row = or.getList().get(0);
        	Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
        	mutator.addInsertion(row.getKey().toString(), userColumnFamily, 
        						 HFactory.createColumn(isLoggedColumn, CassandraHelper.serialize(Boolean.TRUE), ss, bs));	
        	mutator.execute();  
        	logger.debug("login sucessful for " + email + "/" + password
					+ "->userId=" + row.getKey());       	
        	return (String)  row.getKey();
        }
        else{
        	logger.debug("login failed for " + email + "/" + password);
			return null;
        }
	}

	@Override
	public void logout(String userId) {
		SliceQuery<String, String, byte[]> q = HFactory.createSliceQuery(keyspace, ss, ss, bs);
		
		q.setColumnFamily(userColumnFamily);
		q.setKey(userId);
		q.setColumnNames(isLoggedColumn);
		
		QueryResult<ColumnSlice<String, byte[]>> result = q.execute();
		ColumnSlice<String,byte[]> cs = result.get();
		
		if(cs.getColumns().size() != 0){
			Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
        	mutator.addInsertion(userId, userColumnFamily, 
        						 HFactory.createColumn(isLoggedColumn, CassandraHelper.serialize(Boolean.FALSE), ss, bs));	
        	mutator.execute();  
        	logger.debug("User "+userId+" successfully logout");
		}
	}

	@Override
	public void flushUsers() {
		// TODO Auto-generated method stub
		
	}

}
