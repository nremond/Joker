package cl.own.usi.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.dao.ScoreDAO;
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
	
	@Autowired
	ScoreDAO scoreDAO;
	
	private static final int CORRECT_QUESTION_BONUS = 100;

	private static final int FIFTY = 50;
	private static final int HUNDRED = 100;

	public int updateScore(String userId, long deltaTimeToAnswer, boolean answerCorrect) {
		User user = userService.getUserFromUserId(userId);
		if (answerCorrect) {
			int timeBonus = Long.valueOf(deltaTimeToAnswer).intValue();
			user.setScore(user.getScore() + CORRECT_QUESTION_BONUS + timeBonus);
		}
		scoreDAO.updateScore(user);
		return user.getScore();
	}

	public List<User> getTop100() {
		return scoreDAO.getTop(HUNDRED);
	}

	public List<User> get50Before(User user) {
		return scoreDAO.getBefore(user, FIFTY);
	}

	public List<User> get50After(User user) {
		return scoreDAO.getAfter(user, FIFTY);
	}

}
