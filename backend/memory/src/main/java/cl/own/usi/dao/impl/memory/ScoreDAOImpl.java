package cl.own.usi.dao.impl.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import cl.own.usi.dao.ScoreDAO;
import cl.own.usi.dao.UserDAO;
import cl.own.usi.model.User;

@Repository
public class ScoreDAOImpl implements ScoreDAO {

	@Autowired
	private UserDAO userDAO;

	private ConcurrentSkipListSet<User> rankedUsers = new ConcurrentSkipListSet<User>(
			new User.UserComparator());
	private ConcurrentMap<User, Integer> userBonuses = new ConcurrentHashMap<User, Integer>();

	public List<User> getTop(int limit) {
		List<User> users = new ArrayList<User>(limit);
		int i = 0;
		for (User user : rankedUsers) {
			if (i++ < limit) {
				users.add(user);
			} else {
				break;
			}
		}
		return users;
	}

	public List<User> getBefore(User user, int limit) {
		List<User> users = new ArrayList<User>(limit);
		NavigableSet<User> usersBefore = rankedUsers.headSet(user, false);
		int i = 0;
		for (User userBefore : usersBefore) {
			if (userBefore.getScore() > user.getScore()) {
				if (i < limit) {
					users.add(userBefore);
				} else {
					break;
				}
			}
		}
		return users;
	}

	public List<User> getAfter(User user, int limit) {
		List<User> users = new ArrayList<User>(limit);
		NavigableSet<User> usersAfter = rankedUsers.tailSet(user, false);
		int i = 0;
		for (User userAfter : usersAfter) {
			if (userAfter.getScore() < user.getScore()) {
				if (i++ < limit) {
					users.add(userAfter);
				} else {
					break;
				}
			}
		}
		return users;
	}

	public int getUserBonus(String userId) {
		User user = userDAO.getUserById(userId);
		Integer bonus = userBonuses.get(user);
		if (bonus == null) {
			return 0;
		} else {
			return bonus;
		}
	}

	@Override
	public void flushUsers() {
		rankedUsers.clear();
		userBonuses.clear();
	}

	@Override
	public int setBadAnswer(String userId) {
		User user = userDAO.getUserById(userId);
		userBonuses.put(user, 0);
		return user.getScore();
	}

	@Override
	public int setGoodAnswer(String userId, int questionValue) {
		User user = userDAO.getUserById(userId);
		Integer bonus = userBonuses.get(user);
		bonus = (bonus == null) ? 0 : bonus;

		userBonuses.put(user, bonus + 1);
		int newScore = user.getScore() + questionValue + bonus;

		if (rankedUsers.contains(user)) {
			rankedUsers.remove(user);
		}
		user.setScore(newScore);
		rankedUsers.add(user);

		return newScore;
	}

}
