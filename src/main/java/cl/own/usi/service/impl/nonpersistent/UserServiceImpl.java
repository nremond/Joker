package cl.own.usi.service.impl.nonpersistent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.model.User;
import cl.own.usi.service.GameService;
import cl.own.usi.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	GameService gameService;
	
	ConcurrentMap<String, User> users = new ConcurrentHashMap<String, User>();
	
	public boolean insertUser(String email, String password, String firstname,
			String lastname) {
		
		User user = new User();
		user.setEmail(email);
		user.setPassword(password);
		user.setFirstname(firstname);
		user.setLastname(lastname);
		
		User oldUser = users.putIfAbsent(user.getEmail(), user);
		
		if (oldUser != null) {
			return false;
		} else {
			return true;
		}
		
	}

	public String login(String email, String password) {
		
		User user = users.get(email);
		if (user != null) {
			if (user.getPassword().equals(password)) {
				return generateUserId(user);
			}
		}
		return null;
	}

	public boolean insertAnswer(String userId, Integer answer) {
		int correctAnswer = gameService.getCurrentQuestion().getCorrectChoice();
		
		if (correctAnswer == answer) {
			return true;
		} else {
			return false;
		}
	}

	public boolean logout(String userId) {
		return true;
	}

	public User getUserFromUserId(String userId) {
		return users.get(userId);
	}

	private String generateUserId(User user) {
		return user.getEmail();
	}

	public void flushUsers() {
		users.clear();
	}
}
