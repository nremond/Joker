package cl.own.usi.gateway.client;

import java.util.List;

public class BeforeAndAfterScores {
	
	private final List<UserInfoAndScore> scoresBefore;
	private final List<UserInfoAndScore> scoresAfter;
	
	public BeforeAndAfterScores(final List<UserInfoAndScore> scoresBefore, List<UserInfoAndScore> scoresAfter) {
		this.scoresBefore = scoresBefore;
		this.scoresAfter = scoresAfter;
	}

	public List<UserInfoAndScore> getScoresBefore() {
		return scoresBefore;
	}

	public List<UserInfoAndScore> getScoresAfter() {
		return scoresAfter;
	}

}