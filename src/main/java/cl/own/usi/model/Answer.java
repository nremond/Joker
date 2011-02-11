package cl.own.usi.model;

public class Answer {

	User user;
	Question question;
	int answerNumber = -1;
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public Question getQuestion() {
		return question;
	}
	
	public void setQuestion(Question question) {
		this.question = question;
	}
	
	public int getAnswerNumber() {
		return answerNumber;
	}
	
	public void setAnswerNumber(int answerNumber) {
		this.answerNumber = answerNumber;
	}
	
}
