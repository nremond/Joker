package cl.own.usi.model;

import java.util.List;

/**
 * Game representation
 * 
 * @author bperroud
 *
 */
public class Game {

	private int usersLimit;
	private int questionTimeLimit;
	private int pollingTimeLimit;

	private List<Question> questions;

	public int getUsersLimit() {
		return usersLimit;
	}

	public void setUsersLimit(int usersLimit) {
		this.usersLimit = usersLimit;
	}

	public int getQuestionTimeLimit() {
		return questionTimeLimit;
	}

	public void setQuestionTimeLimit(int questionTimeLimit) {
		this.questionTimeLimit = questionTimeLimit;
	}

	public int getPollingTimeLimit() {
		return pollingTimeLimit;
	}

	public void setPollingTimeLimit(int pollingTimeLimit) {
		this.pollingTimeLimit = pollingTimeLimit;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

}
