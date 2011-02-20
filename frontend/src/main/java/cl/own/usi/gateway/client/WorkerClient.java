package cl.own.usi.gateway.client;

import java.util.List;

public interface WorkerClient {
	
	UserAndScore validateUserAndInsertQuestionRequest(String userId, int questionNumber);
	
	UserAndScoreAndAnswer validateUserAndInsertQuestionResponseAndUpdateScore(String userId, int questionNumber, Integer answer);
	
	public class UserAndScore {
		public String userId;
		public int score;
	}
	
	public class UserAndScoreAndAnswer extends UserAndScore {
		public boolean answer;
	}
	
	UserAndScore validateUserAndGetScore(String userId);
	
	String loginUser(String email, String password);
	
	boolean insertUser(String email, String password, String firstname, String lastname);
	
	void flushUsers();
	
	List<UserInfoAndScore> getTop100();
	List<UserInfoAndScore> get50Before(String userId);
	List<UserInfoAndScore> get50After(String userId);
	
	public class UserInfoAndScore extends UserAndScore {
		public String email;
		public String firstname;
		public String lastname;
	}
}
