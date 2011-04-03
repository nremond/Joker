package cl.own.usi.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Object that hold the answer to the audit request for a given question. Object
 * contains the user answer, the good answer, and the question asked.
 *
 * @author reynald
 *
 */
public class AuditAnswer {

	private int userAnswer;
	private int goodAnswer;
	private String question;

	public AuditAnswer(final int userAnswer, final int goodAnswer,
			final String question) {
		super();
		this.userAnswer = userAnswer;
		this.goodAnswer = goodAnswer;
		this.question = question;
	}

	@JsonProperty(value = "user_answer")
	public int getUserAnswer() {
		return userAnswer;
	}

	@JsonProperty(value = "good_answer")
	public int getGoodAnswer() {
		return goodAnswer;
	}

	@JsonProperty(value = "question")
	public String getQuestion() {
		return question;
	}

}
