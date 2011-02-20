package cl.own.usi.json;

import org.codehaus.jackson.annotate.JsonProperty;

public class AnswerResponse {

	private boolean areURight;
	private String goodAnswer;
	private int score;

	public AnswerResponse(boolean areURight, String goodAnswer, int score) {
		super();
		this.areURight = areURight;
		this.goodAnswer = goodAnswer;
		this.score = score;
	}

	@JsonProperty(value = "are_u_right")
	public boolean isAreURight() {
		return areURight;
	}

	@JsonProperty(value = "good_answer")
	public String getGoodAnswer() {
		return goodAnswer;
	}

	public int getScore() {
		return score;
	}
}
