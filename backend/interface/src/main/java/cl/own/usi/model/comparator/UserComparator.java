package cl.own.usi.model.comparator;

import java.util.Comparator;

import cl.own.usi.model.User;

/**
 * Compare Users regarding their lastname, firstname and email.
 * 
 * @author bperroud
 *
 */
public class UserComparator implements Comparator<User> {

	@Override
	public int compare(User o1, User o2) {
		int lastnameCompare = o1.getLastname().compareTo(o2.getLastname());
		if (lastnameCompare == 0) {
			int firstnameCompare = o1.getFirstname().compareTo(o2.getFirstname());
			if (firstnameCompare == 0) {
				return o2.getEmail().compareTo(o1.getEmail());
			} else {
				return firstnameCompare;
			}
		} else {
			return lastnameCompare;
		}
	}
}