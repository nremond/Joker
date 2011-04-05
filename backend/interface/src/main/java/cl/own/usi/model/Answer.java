package cl.own.usi.model;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Answer {

	private String userId;
	private int questionNumber;
	private int answerNumber = -1;

	public Answer() {
		super();
	}

	public Answer(String userId, int questionNumber, int answerNumber) {
		this();
		this.userId = userId;
		this.questionNumber = questionNumber;
		this.answerNumber = answerNumber;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getQuestionNumber() {
		return questionNumber;
	}

	public void setQuestionNumber(int questionNumber) {
		this.questionNumber = questionNumber;
	}

	public int getAnswerNumber() {
		return answerNumber;
	}

	public void setAnswerNumber(int answerNumber) {
		this.answerNumber = answerNumber;
	}

	@Override
	public String toString() {
		// For debugging purpose
		return ToStringBuilder.reflectionToString(this);
	}

}
