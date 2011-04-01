package cl.own.usi.gateway.client;

public class ExtendedUserInfoAndScore extends UserInfoAndScore {

	private final boolean isLogged;
	private final int latestQuestionNumberAnswered;
	
	public ExtendedUserInfoAndScore(final String userId, final int score,
			final String email, final String firstname, final String lastname, final boolean isLogged, final int latestQuestionNumberAnswered) {
		super(userId, score, email, firstname, lastname);
		this.isLogged = isLogged;
		this.latestQuestionNumberAnswered = latestQuestionNumberAnswered;
	}

	public boolean isLogged() {
		return isLogged;
	}

	public int getLatestQuestionNumberAnswered() {
		return latestQuestionNumberAnswered;
	}
	
}