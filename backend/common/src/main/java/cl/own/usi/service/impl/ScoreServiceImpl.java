package cl.own.usi.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.dao.ScoreDAO;
import cl.own.usi.model.User;
import cl.own.usi.service.ScoreService;

@Service
public class ScoreServiceImpl implements ScoreService {

	@Autowired
	private ScoreDAO scoreDAO;

	private static final int FIFTY = 5;
	private static final int HUNDRED = 100;

	public int updateScore(int questionNumber, int answer,
			String userId, boolean answerCorrect) {

		if (answerCorrect) {
			return scoreDAO
					.setGoodAnswer(userId, questionNumber, answer);
		} else {
			return scoreDAO.setBadAnswer(userId, questionNumber, answer);
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

	@Override
	public void computeRankings() {
		scoreDAO.computeRankings();
	}

}
