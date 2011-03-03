package cl.own.usi.dao.impl.cassandra;

import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.emailsColumnFamily;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.usersColumnFamily;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.emailColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.firstnameColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.lastnameColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.passwordColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.scoreColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.isLoggedColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.answerNumberColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.questionNumberColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.userIdColumn;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import me.prettyprint.cassandra.serializers.BooleanSerializer;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HSuperColumn;
import me.prettyprint.hector.api.beans.SuperSlice;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import me.prettyprint.hector.api.query.SuperSliceQuery;

import org.apache.commons.lang.builder.ToStringBuilder;
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
	final static ByteBufferSerializer bbs = ByteBufferSerializer.get();
	final static IntegerSerializer is = IntegerSerializer.get();
	final static BooleanSerializer bs = BooleanSerializer.get();

	@Override
	public boolean insertUser(User user) {
		try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
          
            String userID = CassandraHelper.generateUserId(user);
            
            //Add the user in the CF Users
            mutator.addInsertion(userID, usersColumnFamily, HFactory.createColumn(emailColumn, ss.toByteBuffer(user.getEmail()), ss, bbs));
            mutator.addInsertion(userID, usersColumnFamily, HFactory.createColumn(firstnameColumn, ss.toByteBuffer(user.getFirstname()), ss, bbs));
			mutator.addInsertion(userID, usersColumnFamily, HFactory.createColumn(lastnameColumn, ss.toByteBuffer(user.getLastname()), ss, bbs));
			mutator.addInsertion(userID, usersColumnFamily, HFactory.createColumn(passwordColumn, ss.toByteBuffer(user.getPassword()), ss, bbs));
			mutator.addInsertion(userID, usersColumnFamily, HFactory.createColumn(scoreColumn, is.toByteBuffer(user.getScore()), ss, bbs));
			mutator.addInsertion(userID, usersColumnFamily, HFactory.createColumn(isLoggedColumn, bs.toByteBuffer(Boolean.FALSE), ss, bbs));
			
			//Add the email in the CF Emails (inverted index)
			mutator.addInsertion(user.getEmail(), emailsColumnFamily, HFactory.createColumn(userIdColumn, ss.toByteBuffer(userID), ss, bbs));
			mutator.addInsertion(user.getEmail(), emailsColumnFamily, HFactory.createColumn(passwordColumn, ss.toByteBuffer(user.getPassword()), ss, bbs));
			
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
		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(keyspace, ss, ss, bbs);	
		q.setKey(userId);
		q.setColumnFamily(usersColumnFamily);
		q.setColumnNames(emailColumn,firstnameColumn,lastnameColumn,passwordColumn,scoreColumn);
		
		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
		ColumnSlice<String,ByteBuffer> cs = result.get();
		
		if(cs.getColumns().size() != 0){
			User user = new User();	
			user.setEmail(ss.fromByteBuffer(cs.getColumnByName(emailColumn).getValue()));
			user.setFirstname(ss.fromByteBuffer(cs.getColumnByName(firstnameColumn).getValue()));
			user.setLastname(ss.fromByteBuffer(cs.getColumnByName(lastnameColumn).getValue()));
			user.setPassword(ss.fromByteBuffer(cs.getColumnByName(passwordColumn).getValue()));
			user.setScore(is.fromByteBuffer(cs.getColumnByName(scoreColumn).getValue()));
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

		System.out.println("insertAnswwer ("+answer.getAnswerNumber()+","+
											 answer.getQuestionNumber()+","+
											 answer.getUserId()+")");
		try{
			Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
	
			List<HColumn<String, ByteBuffer>> columnList = new ArrayList<HColumn<String,ByteBuffer>>();
			columnList.add(HFactory.createColumn(answerNumberColumn, is.toByteBuffer(answer.getAnswerNumber()), ss, bbs));
			columnList.add(HFactory.createColumn(questionNumberColumn, is.toByteBuffer(answer.getQuestionNumber()), ss, bbs));	
			mutator.addInsertion(answer.getUserId(), "Answers",HFactory.createSuperColumn(UUID.randomUUID().toString(), columnList, ss, ss, bbs));
			
			mutator.execute();  
			logger.debug("answer inserted, "
					+ ToStringBuilder.reflectionToString(answer));
			
		} catch (HectorException e) {
            e.printStackTrace();
            logger.error("An error occured while inserting answer"+e);  
        }	
	}

	@Override
	public List<Answer> getAnswers(String userId) {
		
		SuperSliceQuery<String, String, String,ByteBuffer> query = HFactory.createSuperSliceQuery(keyspace, ss, ss, ss, bbs);
		query.setColumnFamily("Answers");
		query.setKey(userId);
		query.setRange("", "", false, 100);
		
		QueryResult<SuperSlice<String,String,ByteBuffer>> result = query.execute();
		List<HSuperColumn<String, String, ByteBuffer>> sc_list = result.get().getSuperColumns();
		
		if(sc_list.size() == 0){
			return Collections.emptyList();
		}
		
		List<Answer> answers = new ArrayList<Answer>(sc_list.size());
		for(HSuperColumn<String, String, ByteBuffer> sc: sc_list){
			Answer answer = new Answer();
			answer.setAnswerNumber(is.fromByteBuffer(sc.getColumns().get(0).getValue()));
			answer.setQuestionNumber(is.fromByteBuffer(sc.getColumns().get(1).getValue()));
			answer.setUserId(userId);
			answers.add(answer);
		}
		
		return answers;	
	}

	@Override
	public String login(String email, String password) {	
		
		//Look for the userId in the reverted index
		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(keyspace, ss, ss, bbs);	
		q.setKey(email);
		q.setColumnFamily(emailsColumnFamily);
		q.setColumnNames(userIdColumn,passwordColumn);
		
		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
		ColumnSlice<String,ByteBuffer> cs = result.get();
		
		if(cs.getColumns().size() != 0){
			String userID = ss.fromByteBuffer(cs.getColumnByName(userIdColumn).getValue());
			String passwordFromDB = ss.fromByteBuffer(cs.getColumnByName(passwordColumn).getValue());
			
			if(password.equals(passwordFromDB)){
				Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
	        	mutator.addInsertion(userID, usersColumnFamily, 
	        						 HFactory.createColumn(isLoggedColumn, bs.toByteBuffer(Boolean.TRUE), ss, bbs));	
	        	mutator.execute();  
	        	logger.debug("login sucessful for " + email + "/" + password
						+ "->userId=" + userID);       	
	        	return userID;
			}
		}
		logger.debug("login failed for " + email + "/" + password);
		return null;
	}

	@Override
	public void logout(String userId) {
		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(keyspace, ss, ss, bbs);
		
		q.setColumnFamily(usersColumnFamily);
		q.setKey(userId);
		q.setColumnNames(isLoggedColumn);
		
		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
		ColumnSlice<String,ByteBuffer> cs = result.get();
		
		if(cs.getColumns().size() != 0){
			Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
        	mutator.addInsertion(userId, usersColumnFamily, 
        						 HFactory.createColumn(isLoggedColumn, bs.toByteBuffer(Boolean.FALSE), ss, bbs));	
        	mutator.execute();  
        	logger.debug("User "+userId+" successfully logout");
		}
	}

	@Override
	public void flushUsers() {
		// TODO Auto-generated method stub
	}

}
