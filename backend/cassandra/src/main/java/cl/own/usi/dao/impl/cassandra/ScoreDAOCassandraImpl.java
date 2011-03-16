package cl.own.usi.dao.impl.cassandra;

import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.usersColumnFamily;
import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.scoreColumn;

import java.nio.ByteBuffer;
import java.util.List;

import me.prettyprint.cassandra.serializers.BooleanSerializer;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
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
	public void updateScore(User user, int newScore) {
		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(
				keyspace, ss, ss, bbs);

		q.setColumnFamily(usersColumnFamily);
		q.setKey(user.getUserId());
		q.setColumnNames(scoreColumn);

		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
		ColumnSlice<String, ByteBuffer> cs = result.get();

		if (cs.getColumns().size() != 0) {
			Mutator<String> mutator = HFactory.createMutator(keyspace,
					StringSerializer.get());
			mutator.addInsertion(
					user.getUserId(),
					usersColumnFamily,
					HFactory.createColumn(scoreColumn,
							is.toByteBuffer(newScore), ss, bbs));
			mutator.execute();
			logger.debug("Score of user {} updated to {}", user.getEmail(),
					newScore);
		} else {
			logger.debug("User {} was not found in DB", user.getEmail());
		}

		top100ScoreDAO.setNewScore(user, newScore);
	}

	@Override
	public List<User> getTop(int limit) {
		return top100ScoreDAO.getTop100();
	}

	@Override
	public List<User> getBefore(User user, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> getAfter(User user, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flushUsers() {
		top100ScoreDAO.flushUsers();
	}

	@Override
	public int setBadAnswer(String userId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setGoodAnswer(String userId, int questionValue) {
		// TODO Auto-generated method stub
		return 0;
	}

}
