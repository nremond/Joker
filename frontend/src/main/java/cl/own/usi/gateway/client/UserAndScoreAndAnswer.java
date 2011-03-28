package cl.own.usi.gateway.client;

public class UserAndScoreAndAnswer extends UserAndScore {
	private boolean answer;

	public UserAndScoreAndAnswer(final String userId, final int score,
			final boolean answer) {
		super(userId, score);
		this.answer = answer;
	}

	public boolean isAnswer() {
		return answer;
	}
}