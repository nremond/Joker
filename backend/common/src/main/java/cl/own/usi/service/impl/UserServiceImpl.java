package cl.own.usi.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.dao.ScoreDAO;
import cl.own.usi.dao.UserDAO;
import cl.own.usi.model.Answer;
import cl.own.usi.model.User;
import cl.own.usi.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private ScoreDAO scoreDAO;

	public boolean insertUser(String email, String password, String firstname,
			String lastname) {

		if (email == null || password == null || firstname == null || lastname == null) {
			throw new IllegalArgumentException("an argument is null.");
		} else {
			User user = new User();
			user.setEmail(email);
			user.setPassword(password);
			user.setFirstname(firstname);
			user.setLastname(lastname);

			return userDAO.insertUser(user);
		}
	}

	public String login(String email, String password) {

		if (email == null || password == null) {
			return null;
		} else {
			return userDAO.login(email, password);
		}
	}

	//TODO String userId in the signature
	public void insertRequest(User user, int questionNumber) {
		userDAO.insertRequest(user.getUserId(), questionNumber);
	}

	//TODO String userId in the signature
	public void insertAnswer(User user, int questionNumber, Integer answerNumber) {

		List<Answer> answers = userDAO.getAnswers(user.getUserId());

		if (answers.get(questionNumber - 1) != null) {
			throw new IllegalArgumentException("User has already answered this question.");
		} else {

			Answer answer = new Answer();
			answer.setQuestionNumber(questionNumber);
			answer.setUserId(user.getUserId());
			answer.setAnswerNumber(answerNumber);

			userDAO.insertAnswer(answer);

		}
	}

	public boolean logout(String userId) {
		User user = getUserFromUserId(userId);
		if (user == null) {
			return false;
		} else {
			userDAO.logout(userId);
			return true;
		}
	}

	public User getUserFromUserId(String userId) {
		return userDAO.getUserById(userId);
	}

	public void flushUsers() {
		userDAO.flushUsers();
		scoreDAO.flushUsers();
	}

}
