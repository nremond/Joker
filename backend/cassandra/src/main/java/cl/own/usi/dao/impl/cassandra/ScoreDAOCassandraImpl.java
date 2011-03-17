package cl.own.usi.dao.impl.cassandra;

import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.emailColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.firstnameColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.lastnameColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.passwordColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.userIdColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.usersColumnFamily;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.scoreColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.bonusColumn;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.scoreColumnFamily;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import me.prettyprint.cassandra.serializers.BooleanSerializer;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
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
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import cl.own.usi.dao.ScoreDAO;
import cl.own.usi.dao.Top100ScoreDAO;
import cl.own.usi.model.User;

@Repository
public class ScoreDAOCassandraImpl implements ScoreDAO {

	@Autowired
	Cluster cluster;

	@Autowired
	Keyspace keyspace;

	@Autowired
	Top100ScoreDAO top100ScoreDAO;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	final StringSerializer ss = StringSerializer.get();
	final ByteBufferSerializer bbs = ByteBufferSerializer.get();
	final IntegerSerializer is = IntegerSerializer.get();
	final BooleanSerializer bs = BooleanSerializer.get();


	@Override
	public List<User> getTop(int limit) {
		return top100ScoreDAO.getTop100();
	}
	
	private List<User> executeRangeSlicesQuery(RangeSlicesQuery<String, String, ByteBuffer> rangeSlicesQuery, int limit){
		List<User> users = new ArrayList<User>(limit);
		QueryResult<OrderedRows<String, String, ByteBuffer>> result = rangeSlicesQuery.execute();
        OrderedRows<String, String, ByteBuffer> orderedRows = result.get();
        int i = 0;
        for (Row<String, String, ByteBuffer> r : orderedRows) {
            String userID = ss.fromByteBuffer(r.getColumnSlice().getColumnByName(userIdColumn).getValue());
            if (i++ < limit){
            	users.add(getUserById(userID));
            }else{
            	break;
            } 	
        }        
		return users;
	}

	@Override
	public List<User> getBefore(User user, int limit) {

		RangeSlicesQuery<String, String, ByteBuffer> rangeSlicesQuery = HFactory.createRangeSlicesQuery(keyspace, ss, ss, bbs);
        rangeSlicesQuery.setColumnFamily(scoreColumnFamily);  
        rangeSlicesQuery.setColumnNames(userIdColumn);
        rangeSlicesQuery.setKeys("", user.getScore()+"_"+user.getUserId());
        
        return executeRangeSlicesQuery(rangeSlicesQuery, limit);
	}

	@Override
	public List<User> getAfter(User user, int limit) {
		RangeSlicesQuery<String, String, ByteBuffer> rangeSlicesQuery = HFactory.createRangeSlicesQuery(keyspace, ss, ss, bbs);
        rangeSlicesQuery.setColumnFamily(scoreColumnFamily);      
        rangeSlicesQuery.setColumnNames(userIdColumn);
        rangeSlicesQuery.setKeys(user.getScore()+"_"+user.getUserId(), "");
        
        return executeRangeSlicesQuery(rangeSlicesQuery, limit);
	}
	
	private User getUserById(String userId) {	
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
			logger.debug("fetching userId={} is impossible, not in db", userId);
			return null;
		}
	}

	@Override
	public void flushUsers() {
		top100ScoreDAO.flushUsers();
	}

	@Override
	public int setBadAnswer(String userId) {
		int score = 0;
		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(keyspace, ss, ss, bbs);	
		q.setKey(userId);
		q.setColumnFamily(usersColumnFamily);
		q.setColumnNames(scoreColumn);
		
		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
		ColumnSlice<String,ByteBuffer> cs = result.get();
		
		if(cs.getColumns().size() != 0){
			score = is.fromByteBuffer(cs.getColumnByName(scoreColumn).getValue());
		}
		try{
			//Reset Bonus to zero
		    Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
	        mutator.addInsertion(userId, usersColumnFamily, HFactory.createColumn(bonusColumn, is.toByteBuffer(0), ss, bbs));
	        mutator.execute();  
	    } catch (HectorException e) {
	        logger.error("An error occured while resetting user bonus", e);
	        return score;
	    }
	    logger.debug("setBadAnswer for user {} whose score is now {}", userId,
				score);
		return score;
	}

	@Override
	public int setGoodAnswer(String userId, int questionValue) {
		int score = 0;
		int newScore = 0;
		int bonus = 0;
		
		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(keyspace, ss, ss, bbs);	
		q.setKey(userId);
		q.setColumnFamily(usersColumnFamily);
		q.setColumnNames(scoreColumn,bonusColumn);
		
		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
		ColumnSlice<String,ByteBuffer> cs = result.get();
		
		if(cs.getColumns().size() != 0){
			score = is.fromByteBuffer(cs.getColumnByName(scoreColumn).getValue());
			bonus = is.fromByteBuffer(cs.getColumnByName(bonusColumn).getValue());
		}
		
		bonus++;
		newScore= score + bonus + questionValue;
		
		try{
		    Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
	        
		    //Set new score and bonus to CF Users
	        mutator.addInsertion(userId, usersColumnFamily, HFactory.createColumn(scoreColumn, is.toByteBuffer(newScore), ss, bbs));
	        mutator.addInsertion(userId, usersColumnFamily, HFactory.createColumn(bonusColumn, is.toByteBuffer(bonus), ss, bbs));
			
	        //Set the userID corresponding to the score in the CF Score
			mutator.addInsertion(score+"_"+userId, scoreColumnFamily, HFactory.createColumn(userIdColumn, ss.toByteBuffer(userId), ss, bbs));
			
	        mutator.execute();  
	        logger.debug(
					"setGoodAnswer for user {} whose previous score was {} and is now {}",
					new Object[] { userId, score, newScore });

	    } catch (HectorException e) {
	        logger.error("An error occured while inserting user", e);
	        return 0;
	    }
		return newScore;
	}

}
