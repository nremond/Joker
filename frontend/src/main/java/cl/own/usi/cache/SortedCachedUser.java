package cl.own.usi.cache;

public class SortedCachedUser implements Comparable<SortedCachedUser> {

	private static final char DOT = '.';
	private static final char AT = '@';
	
	private final String lastname;
	private final String firstname;
	private final String email;
	private final int score;
		
	public SortedCachedUser(final String lastname, final String firstname, final String email, final int score) {
		this.lastname = lastname;
		this.firstname = firstname;
		final int indexOfAt = email.indexOf(AT);
		if (indexOfAt > -1) {
			this.email = email.substring(indexOfAt + 1);
		} else {
			this.email = email;
		}
		this.score = score;
	}

	public String getLastname() {
		return lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getFullEmail() {
		return getFirstname() + DOT + getLastname() + AT + getEmail();
	}
	
	protected String getEmail() {
		return email;
	}

	public int getScore() {
		return score;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object instanceof SortedCachedUser) {
			SortedCachedUser other = (SortedCachedUser)object;
			return compareTo(other) == 0;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(SortedCachedUser other) {
		if (score > other.score) {
			return -1;
		} else if (score < other.score) {
			return 1;
		} else {
			int compare = getLastname().compareTo(other.getLastname());
			if (compare == 0) {
				compare = getFirstname().compareTo(other.getFirstname());
				if (compare == 0) {
					return getEmail().compareTo(other.getEmail());
				} else {
					return compare;
				}
			} else {
				return compare;
			}
		}
	}
	
}
