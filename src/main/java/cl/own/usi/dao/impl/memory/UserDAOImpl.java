package cl.own.usi.dao.impl.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.base64.Base64Dialect;
import org.jboss.netty.util.CharsetUtil;
import org.springframework.stereotype.Repository;
import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import cl.own.usi.dao.UserDAO;

import cl.own.usi.model.Answer;
import cl.own.usi.model.Question;
import cl.own.usi.model.User;

@Repository
public class UserDAOImpl implements UserDAO {

	private static final String USER_ID_SALT = "123456";
	
	private ConcurrentMap<String, User> users = new ConcurrentHashMap<String, User>();
	private ConcurrentMap<User, LinkedList<RequestAndAnswer>> userRequestAndAnswers = new ConcurrentHashMap<User, LinkedList<RequestAndAnswer>>();
	private ConcurrentMap<String, User> loggedUsers = new ConcurrentHashMap<String, User>();
	
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

	public void logout(User user) {
		loggedUsers.remove(generateUserId(user));
	}

	public void flushUsers() {
		
		loggedUsers.clear();
		userRequestAndAnswers.clear();
		users.clear();
		
	}

	public void insertRequest(User user, int questionNumber) {
		LinkedList<RequestAndAnswer> requestAndAnswers = userRequestAndAnswers.get(user);
		
		if (requestAndAnswers == null) {
			requestAndAnswers = new LinkedList<RequestAndAnswer>();
			LinkedList<RequestAndAnswer> tmpAnswers = userRequestAndAnswers.putIfAbsent(user, requestAndAnswers);
			if (tmpAnswers != null) {
				requestAndAnswers = tmpAnswers;
			}
		}
		
		RequestAndAnswer requestAndAnswer = new RequestAndAnswer();
		requestAndAnswer.questionNumber = questionNumber;
		requestAndAnswers.add(requestAndAnswer);
	}
	
	public void insertAnswer(User user, Answer answer) {
		
		LinkedList<RequestAndAnswer> requestAndAnswers = userRequestAndAnswers.get(user);
		
		if (requestAndAnswers != null) {
			RequestAndAnswer lastRequestAndAnswer = requestAndAnswers.getLast();
			if (lastRequestAndAnswer.questionNumber == answer.getQuestionNumber()) {
				lastRequestAndAnswer.answer = answer;
			}
		}
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
		ChannelBuffer chanBuff = wrappedBuffer((user.getEmail() + USER_ID_SALT).getBytes(CharsetUtil.UTF_8));
		return Base64.encode(chanBuff, Base64Dialect.ORDERED).toString(CharsetUtil.UTF_8);
	}

	public List<Answer> getAnswers(User user) {
		
		List<RequestAndAnswer> requestAndAnswers = userRequestAndAnswers.get(user);
		
		if (requestAndAnswers == null) {
			return Collections.emptyList();
		} else {
			List<Answer> answers = new ArrayList<Answer>();
			for (RequestAndAnswer raa : requestAndAnswers) {
				answers.add(raa.answer);
			}
			return answers;
		}
	}
	
	private static class RequestAndAnswer {
		int questionNumber;
		Answer answer;
	}

}
