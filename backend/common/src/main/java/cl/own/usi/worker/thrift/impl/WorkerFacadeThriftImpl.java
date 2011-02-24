package cl.own.usi.worker.thrift.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.model.User;
import cl.own.usi.service.ScoreService;
import cl.own.usi.service.UserService;
import cl.own.usi.thrift.*;

@Component
public class WorkerFacadeThriftImpl implements WorkerRPC.Iface, InitializingBean {

	@Autowired
	private	UserService userService;

	@Autowired
	private ScoreService scoreService;

	private int port = 7911;

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			TServerSocket serverTransport = new TServerSocket(getPort());
			WorkerRPC.Processor processor =
				new WorkerRPC.Processor(this);
			org.apache.thrift.protocol.TBinaryProtocol.Factory protFactory = new TBinaryProtocol.Factory(
					true, true);

			TServer server = new TThreadPoolServer(processor, serverTransport, protFactory);
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public UserAndScore validateUserAndInsertQuestionRequest(String userId,
			int questionNumber) throws TException {

		UserAndScore userAndScore = new UserAndScore();

		User user = userService.getUserFromUserId(userId);
		if (user != null) {
			userAndScore.userId = userId;
			userAndScore.score = user.getScore();
			userService.insertRequest(userId, questionNumber);
		}

		return userAndScore;
	}

	@Override
	public UserAndScore validateUserAndInsertQuestionResponseAndUpdateScore(
			String userId, int questionNumber, int questionValue, int answer,
			boolean answerCorrect) throws TException {

		UserAndScore userAndScore = new UserAndScore();

		User user = userService.getUserFromUserId(userId);
		if (user != null) {
			userAndScore.userId = userId;
			userService.insertAnswer(userId, questionNumber, answer);
			userAndScore.score = scoreService.updateScore(questionNumber, questionValue, user, answerCorrect);
		}

		return userAndScore;

	}

	@Override
	public UserAndScore validateUserAndGetScore(String userId)
			throws TException {

		UserAndScore userAndScore = new UserAndScore();

		User user = userService.getUserFromUserId(userId);
		if (user != null) {
			userAndScore.userId = userId;
			userAndScore.score = user.getScore();
		}

		return userAndScore;
	}

	@Override
	public String loginUser(String email, String password) throws TException {

		return userService.login(email, password);

	}

	@Override
	public boolean insertUser(String email, String password, String firstname,
			String lastname) throws TException {

		return userService.insertUser(email, password, firstname, lastname);

	}

	@Override
	public void flushUsers() throws TException {
		userService.flushUsers();
	}

	@Override
	public List<cl.own.usi.thrift.UserInfoAndScore> getTop100() throws TException {

		List<User> users = scoreService.getTop100();

		List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(users.size());
		for (User user : users) {
			retUsers.add(map(user));
		}
		return retUsers;

	}

	@Override
	public List<UserInfoAndScore> get50Before(String userId) throws TException {

		User theUser = userService.getUserFromUserId(userId);
		if (theUser != null) {
			List<User> users = scoreService.get50Before(theUser);

			List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(users.size());
			for (User user : users) {
				retUsers.add(map(user));
			}
			return retUsers;
		} else {
			return null;
		}

	}

	@Override
	public List<UserInfoAndScore> get50After(String userId) throws TException {

		User theUser = userService.getUserFromUserId(userId);
		if (theUser != null) {
			List<User> users = scoreService.get50After(theUser);

			List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(users.size());
			for (User user : users) {
				retUsers.add(map(user));
			}
			return retUsers;
		} else {
			return null;
		}
	}

	private cl.own.usi.thrift.UserInfoAndScore map(User user) {
		UserInfoAndScore userInfoAndScore = new UserInfoAndScore();
		userInfoAndScore.email = user.getEmail();
		userInfoAndScore.firstname = user.getFirstname();
		userInfoAndScore.lastname = user.getLastname();
		userInfoAndScore.score = user.getScore();
		userInfoAndScore.userId = null;
		return userInfoAndScore;
	}

}
