package cl.own.usi.worker.thrift.impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.model.User;
import cl.own.usi.network.InetAddressHelper;
import cl.own.usi.service.ScoreService;
import cl.own.usi.service.UserService;
import cl.own.usi.thrift.UserAndScore;
import cl.own.usi.thrift.UserInfoAndScore;
import cl.own.usi.thrift.WorkerRPC;
import cl.own.usi.worker.management.NetworkReachable;

/**
 * Server-side implementation of the Thrift interface.
 *
 * @author bperroud
 *
 */
@Component
public class WorkerFacadeThriftImpl implements WorkerRPC.Iface,
		InitializingBean, NetworkReachable, DisposableBean {

	@Autowired
	private UserService userService;

	@Autowired
	private ScoreService scoreService;

	private int port = 7911;

	private WorkerFacadeThriftThread thriftThread;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WorkerFacadeThriftImpl.class);

	@Override
	public void afterPropertiesSet() throws Exception {

		try {
			final TServerSocket serverTransport = new TServerSocket(port);
			final WorkerRPC.Processor processor = new WorkerRPC.Processor(this);
			final Factory protFactory = new TBinaryProtocol.Factory(true, true);

			final TServer server = new TThreadPoolServer(processor,
					serverTransport, protFactory);

			thriftThread = new WorkerFacadeThriftThread(server);
			thriftThread.start();

		} catch (TTransportException e) {
			LOGGER.warn("Transport exception caught", e);
		}
	}

	public void setPort(final int port) {
		this.port = port;
	}

	InetAddress localAddress = InetAddressHelper.getCurrentIP();

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
		userAndScore.userId = userId;
		userService.insertAnswer(userId, questionNumber, answer);
		userAndScore.score = scoreService.updateScore(questionNumber,
				questionValue, userId, answerCorrect);

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
	public List<UserInfoAndScore> getTop100() throws TException {

		List<User> users = scoreService.getTop100();

		List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(
				users.size());
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

			List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(
					users.size());
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

			List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(
					users.size());
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

	@Override
	public String getHost() {
		return localAddress.getHostAddress();
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void destroy() throws Exception {

		if (thriftThread != null) {
			thriftThread.requestShutdown();
		}
	}

}
