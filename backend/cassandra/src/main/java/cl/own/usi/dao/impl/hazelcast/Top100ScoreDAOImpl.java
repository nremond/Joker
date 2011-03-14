package cl.own.usi.dao.impl.hazelcast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

import cl.own.usi.dao.Top100ScoreDAO;
import cl.own.usi.model.User;

@Repository
public class Top100ScoreDAOImpl implements Top100ScoreDAO, InitializingBean, MessageListener<User> {

	private final ConcurrentSkipListSet<User> rankedUsers = new ConcurrentSkipListSet<User>(new UserComparator());
	private final AtomicInteger countRankedUsers = new AtomicInteger(0);
	
	private ITopic<User> topic;
	
	@Override
	public void setNewScore(User user, int newScore) {
		user.setScore(newScore);
		setNewScore(user, true);
	}

	private void setNewScore(User user, boolean fromInternal) {
		
		try {
			User smallerRankedUser = rankedUsers.last();
			if (user.getScore() >= smallerRankedUser.getScore()) {
				rankedUsers.add(user);
				int count = countRankedUsers.incrementAndGet();
				if (count > 100) {
					Iterator<User> it = rankedUsers.descendingIterator();
					int smallestScore = smallerRankedUser.getScore();
					while (it.hasNext()) {
						User u = it.next();
						if (u.getScore() == smallestScore) {
							rankedUsers.remove(u);
							countRankedUsers.decrementAndGet();
						} else {
							break;
						}
					}
				}
			}
		} catch (NoSuchElementException e) {
			rankedUsers.add(user);
		}
		if (fromInternal) {
			topic.publish(user);
		}
	}
	
	@Override
	public List<User> getTop100() {
		final List<User> users = new ArrayList<User>(100);
		final Iterator<User> it = rankedUsers.iterator();
		int i = 0;
		while (it.hasNext() && i < 100) {
			final User u = it.next();
			users.add(u);
			i++;
		}
		return users;
	}

	@Override
	public void flushUsers() {
		rankedUsers.clear();
		countRankedUsers.set(0);
	}
	
	public static class UserComparator implements Comparator<User> {

		@Override
		public int compare(User o1, User o2) {
			if (o1.getScore() == o2.getScore()) {
				return 0;
			} else if (o1.getScore() > o2.getScore()) {
				return -1;
			} else {
				return 1;
			}

		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		topic = Hazelcast.getTopic("top100Scores");
		topic.addMessageListener(this);
	}

	@Override
	public void onMessage(User user) {
		setNewScore(user, false);
	}
	
}
