package cl.own.usi.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.dao.ScoreDAO;
import cl.own.usi.dao.UserDAO;
import cl.own.usi.model.User;
import cl.own.usi.service.ScoreService;

@Service
public class ScoreServiceImpl implements ScoreService {

	@Autowired
	ScoreDAO scoreDAO;

	@Autowired
	UserDAO userDAO;
	
	private static final int FIFTY = 50;
	private static final int HUNDRED = 100;

	public int updateScore(int questionNumber, int questionValue, String userId,
			boolean answerCorrect) {
		
		User user = userDAO.getUserById(userId);
		
		if (answerCorrect) {
			
			int newScore = user.getScore();
			// TODO can't we refactor in one DAO call ? like
			// increaseUserBonus(userId, delta)
			int bonus = scoreDAO.getUserBonus(user.getUserId());
			scoreDAO.setUserBonus(user, bonus + 1);
			newScore += questionValue + bonus;
			scoreDAO.updateScore(user, newScore);
			return newScore;
		} else {
			scoreDAO.setUserBonus(user, 0);
			return user.getScore();
		}
	
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
