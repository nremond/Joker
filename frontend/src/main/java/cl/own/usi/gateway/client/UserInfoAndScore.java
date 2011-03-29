package cl.own.usi.gateway.client;

public class UserInfoAndScore extends UserAndScore {
	private String email;
	private String firstname;
	private String lastname;

	public UserInfoAndScore(final String userId, final int score,
			final String email, final String firstname, final String lastname) {
		super(userId, score);
		this.email = email;
		this.firstname = firstname;
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}
}