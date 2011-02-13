package cl.own.usi.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.dao.UserDAO;
import cl.own.usi.model.Answer;
import cl.own.usi.model.Question;
import cl.own.usi.model.User;
import cl.own.usi.service.GameService;
import cl.own.usi.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	GameService gameService;
	
	@Autowired
	UserDAO userDAO;
	
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

	public boolean insertRequest(User user, int questionNumber) {
		Question question = gameService.getQuestion(questionNumber);
		userDAO.insertRequest(user, question);
		return true;
	}
	
	public boolean insertAnswer(User user, int questionNumber, Integer answerNumber) {
		Question question = gameService.getQuestion(questionNumber);
		
		if (answerNumber == null) {
			throw new IllegalArgumentException("answerNumber is null.");
		} else {
			
			if (answerNumber > question.getChoices().size() || answerNumber < 1) {
				throw new IllegalArgumentException("answerNumber " + answerNumber + " is out of range of questions choices.");
			} else {
				
				List<Answer> answers = userDAO.getAnswers(user);
				
				// Ensure user has answered previous questions.
				if (question.getNumber() != answers.size()) {
					throw new IllegalArgumentException("User has not answered all previous questions.");
				} else if (answers.get(question.getNumber() - 1) != null) {
					throw new IllegalArgumentException("User has already answered this question.");
				} else {
					
					Answer answer = new Answer();
					answer.setQuestion(question);
					answer.setUser(user);
					
					answer.setAnswerNumber(answerNumber);
					
					userDAO.insertAnswer(user, answer);
					
					if (answer.isCorrect()) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
	}

	public boolean logout(String userId) {
		User user = getUserFromUserId(userId);
		if (user == null) {
			return false;
		} else {
			userDAO.logout(user);
			return true;
		}
	}

	public User getUserFromUserId(String userId) {
		return userDAO.getUserById(userId);
	}

	public void flushUsers() {
		userDAO.flushUsers();
	}

	public boolean isQuestionRequestAllowed(User user, int questionNumber) {
		List<Answer> answers = userDAO.getAnswers(user);
		return answers.size() == questionNumber - 1;
	}
	
	public boolean isQuestionResponseAllowed(User user, int questionNumber) {
		List<Answer> answers = userDAO.getAnswers(user);
		return answers.size() == questionNumber;
	}
	
}
