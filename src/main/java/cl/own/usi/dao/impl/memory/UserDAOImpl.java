package cl.own.usi.dao.impl.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.util.CharsetUtil;
import org.springframework.stereotype.Repository;

import cl.own.usi.dao.UserDAO;
import cl.own.usi.gateway.utils.Base64;
import cl.own.usi.model.Answer;
import cl.own.usi.model.Question;
import cl.own.usi.model.User;

@Repository
public class UserDAOImpl implements UserDAO {

	private static final String USER_ID_SALT = "123456";
	
	ConcurrentMap<String, User> users = new ConcurrentHashMap<String, User>();
	ConcurrentMap<User, List<Answer>> userAnswers = new ConcurrentHashMap<User, List<Answer>>();
	ConcurrentMap<String, User> loggedUsers = new ConcurrentHashMap<String, User>();
	
	public boolean insertUser(User user) {
		User oldUser = users.putIfAbsent(user.getEmail(), user);
		
		if (oldUser != null) {
			return false;
		} else {
			return true;
		}
	}

	public User getUserById(String userId) {
		return loggedUsers.get(userId);
	}

	public boolean insertAnswer(User user, Question question, int answerNumber) {
		
		List<Answer> answers = userAnswers.get(user);
		if (answers == null) {
			answers = new ArrayList<Answer>();
		}
		// TODO Auto-generated method stub
		return false;
	}

	public void logout(User user) {
		loggedUsers.remove(generateUserId(user));
	}

	public void flushUsers() {
		users.clear();
	}

	public void insertAnswer(User user, Answer answer) {
		
		List<Answer> answers = userAnswers.get(user);
		
		if (answers == null) {
			answers = new ArrayList<Answer>();
			List<Answer> tmpAnswers = userAnswers.putIfAbsent(user, answers);
			if (tmpAnswers != null) {
				answers = tmpAnswers;
			}
		}
		answers.add(answer);
	}

	public String login(String email, String password) {
		
		User user = users.get(email);
		if (user != null) {
			String userId = generateUserId(user);
			if (user.getPassword().equals(password)) {
				User tmpUser = loggedUsers.putIfAbsent(userId, user);
				if (tmpUser == null) {
					return userId;
				}
			}
		}
		return null;
	}

	private String generateUserId(User user) {
		return Base64.encodeBytes((user.getEmail() + USER_ID_SALT).getBytes(CharsetUtil.UTF_8), Base64.ORDERED);
	}

	public List<Answer> getAnswers(User user) {
		
		List<Answer> answers = userAnswers.get(user);
		
		if (answers == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(answers);
		}
	}
	
}
