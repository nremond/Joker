package cl.own.usi.model;

public class Answer {

	User user;
	int questionNumber;
	int answerNumber = -1;
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
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
}
