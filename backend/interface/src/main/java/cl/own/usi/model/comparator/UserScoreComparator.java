package cl.own.usi.model.comparator;

import java.util.Comparator;

import cl.own.usi.model.User;

/**
 * Compare Users regarding their scores first, then use UserComparator
 * 
 * @author bperroud
 *
 */
public class UserScoreComparator implements Comparator<User> {
	
	@Override
	public int compare(User o1, User o2) {
		if (o1.getScore() == o2.getScore()) {
			UserComparator comparator = new UserComparator();
			return comparator.compare(o1, o2);
		} else if (o1.getScore() > o2.getScore()) {
			return -1;
		} else {
			return 1;
		}
	}
}
