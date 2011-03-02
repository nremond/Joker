package cl.own.usi.dao.impl.cassandra;

import java.util.List;

import org.springframework.stereotype.Repository;

import cl.own.usi.dao.ScoreDAO;
import cl.own.usi.model.User;

@Repository
public class ScoreDAOCassandraImpl implements ScoreDAO{

	@Override
	public void updateScore(User user, int newScore) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<User> getTop(int limit) {
		// TODO Auto-generated method stub
		return null;
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
	public int getUserBonus(User user) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setUserBonus(User user, int newBonus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flushUsers() {
		// TODO Auto-generated method stub
		
	}

}
