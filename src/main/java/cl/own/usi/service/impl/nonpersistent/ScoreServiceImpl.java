package cl.own.usi.service.impl.nonpersistent;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.model.User;
import cl.own.usi.service.GameService;
import cl.own.usi.service.ScoreService;
import cl.own.usi.service.UserService;

@Service
public class ScoreServiceImpl implements ScoreService {

	@Autowired
	GameService gameService;
	
	@Autowired
	UserService userService;
	
	private static final int CORRECT_QUESTION_BONUS = 100;

	private static final int FIFTY = 50;

	private ConcurrentSkipListSet<User> rankedUsers = new ConcurrentSkipListSet<User>();
	
	public int updateScore(String userId, long deltaTimeToAnswer, boolean answerCorrect) {
		User user = userService.getUserFromUserId(userId);
		if (answerCorrect) {
			int timeBonus = Long.valueOf(deltaTimeToAnswer).intValue();
			user.setScore(user.getScore() + CORRECT_QUESTION_BONUS + timeBonus);
		}
		rankedUsers.add(user);
		return user.getScore();
	}

	public List<User> getTop100() {
		List<User> users = new ArrayList<User>(100);
		int i = 0;
		for (User user : rankedUsers) {
			if (i++ < 100) {
				users.add(user);
			} else {
				break;
			}
		}
		return users;
	}

	public List<User> get50Before(User user) {
		List<User> users = new ArrayList<User>(FIFTY);
		NavigableSet<User> usersBefore = rankedUsers.headSet(user, false);
		int i = 0;
		for (User userBefore : usersBefore) {
			if (i++ < FIFTY) {
				users.add(userBefore);
			} else {
				break;
			}
		}
		return users;
	}

	public List<User> get50After(User user) {
		List<User> users = new ArrayList<User>(FIFTY);
		NavigableSet<User> usersAfter = rankedUsers.tailSet(user, false);
		int i = 0;
		for (User userAfter : usersAfter) {
			if (i++ < FIFTY) {
				users.add(userAfter);
			} else {
				break;
			}
		}
		return users;
	}

}
