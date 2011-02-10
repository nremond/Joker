package cl.own.usi.dao.impl.memory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Repository;

import cl.own.usi.dao.UserDAO;
import cl.own.usi.model.User;

@Repository
public class UserDAOImpl implements UserDAO {

	ConcurrentMap<String, User> users = new ConcurrentHashMap<String, User>();
	
	public boolean insertUser(User user) {
		User oldUser = users.putIfAbsent(user.getEmail(), user);
		
		if (oldUser != null) {
			return false;
		} else {
			return true;
		}
	}

	public User  getUserById(String userId) {
		return users.get(userId);
	}

	public boolean insertAnswer(User user, int questionNumber, String answer) {
		// TODO Auto-generated method stub
		return false;
	}

	public void logout(User user) {
		// TODO Auto-generated method stub

	}

	public void flushUsers() {
		users.clear();
	}

}
