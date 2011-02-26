package cl.own.usi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	String userId;
	String email;
	String password;
	String firstname;
	String lastname;
	int score = 0;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	List<Integer> answers = new ArrayList<Integer>();

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(email).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj.getClass() != getClass()) {
			return false;
		}

		User rhs = (User) obj;
		return new EqualsBuilder().append(email, rhs.email).isEquals();

	}

	@Override
	public String toString() {
		return "[userId:" + userId + ",email:" + email + ",score:" + score
				+ "]";
	}

	public static class UserComparator implements Comparator<User> {

		@Override
		public int compare(User o1, User o2) {
			if (o1.getScore() == o2.getScore()) {
				return o2.getEmail().compareTo(o1.getEmail());
			} else if (o1.getScore() > o2.getScore()) {
				return -1;
			} else {
				return 1;
			}

		}
	}

}
