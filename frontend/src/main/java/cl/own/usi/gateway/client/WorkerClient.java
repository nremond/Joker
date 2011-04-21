package cl.own.usi.gateway.client;

import java.util.List;

import cl.own.usi.model.Game;

/**
 * Client interface to interact with remote workers.
 *
 * @author bperroud
 *
 */
public interface WorkerClient {

	UserAndScore validateUserAndInsertQuestionRequest(String userId,
			int questionNumber);

	UserAndScoreAndAnswer validateUserAndInsertQuestionResponseAndUpdateScore(
			String userId, int questionNumber, Integer answer);

	UserAndScore validateUserAndGetScore(String userId);

	UserLogin loginUser(String email, String password);

	boolean insertUser(String email, String password, String firstname,
			String lastname);

	void flushUsers();

	List<UserInfoAndScore> getTop100();

	@Deprecated
	List<UserInfoAndScore> get50Before(String userId);

	@Deprecated
	List<UserInfoAndScore> get50After(String userId);

	BeforeAndAfterScores get50BeforeAnd50After(String userId);

	boolean addWorkerNode(String host, int port);

	void gameEnded();

	String getAnswersAsJson(String email, Integer questionNumber, Game game);

	String getScoreAsJson(String email);

	ExtendedUserInfoAndScore getExtendedUserInfo(String userId);

	void gameCreated();
	
	void ping();
	
	List<UserInfoAndScore> getUsers(int from, int limit);
	
}
