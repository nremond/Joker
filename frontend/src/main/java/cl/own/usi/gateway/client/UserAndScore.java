package cl.own.usi.gateway.client;

public class UserAndScore {
	private final String userId;
	private final int score;

	public UserAndScore(final String userId, final int score) {
		super();
		this.userId = userId;
		this.score = score;
	}

	public String getUserId() {
		return userId;
	}

	public int getScore() {
		return score;
	}

}