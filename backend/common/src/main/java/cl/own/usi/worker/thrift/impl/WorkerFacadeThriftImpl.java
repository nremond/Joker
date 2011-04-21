package cl.own.usi.worker.thrift.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.exception.UserAlreadyLoggedException;
import cl.own.usi.model.AuditAnswer;
import cl.own.usi.model.AuditAnswers;
import cl.own.usi.model.Scores;
import cl.own.usi.model.User;
import cl.own.usi.network.InetAddressHelper;
import cl.own.usi.service.ScoreService;
import cl.own.usi.service.UserService;
import cl.own.usi.thrift.BeforeAndAfterScores;
import cl.own.usi.thrift.ExtendedUserInfoAndScore;
import cl.own.usi.thrift.UserAndScore;
import cl.own.usi.thrift.UserInfoAndScore;
import cl.own.usi.thrift.UserLogin;
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

	private final InetAddress localAddress = InetAddressHelper.getCurrentIP();

	private final ObjectMapper mapper = new ObjectMapper();

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
				questionValue, answer, userId, answerCorrect);

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
	public UserLogin loginUser(String email, String password) throws TException {

		final UserLogin userLogin = new UserLogin();
		try {
			final String userId = userService.login(email, password);
			userLogin.alreadyLogged = false;
			if (userId != null) {
				userLogin.userId = userId;
			}
		} catch (UserAlreadyLoggedException e) {
			userLogin.alreadyLogged = true;
		}
		return userLogin;

	}

	@Override
	public boolean insertUser(String email, String password, String firstname,
			String lastname) throws TException {

		return userService.insertUser(email, password, firstname, lastname);

	}

	@Override
	public void flushUsers(final int useless) throws TException {
		LOGGER.info("%%% flushUsers() has called by tokyo thrift%%%");
		userService.flushUsers();
	}

	@Override
	public List<UserInfoAndScore> getTop100(final int useless)
			throws TException {

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
		final UserInfoAndScore userInfoAndScore = new UserInfoAndScore();
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
	public void gameEnded(final int useless) {
		scoreService.gameEnded();
	}

	@Override
	public String getScoreAsJson(String email) throws TException {
		LOGGER.debug("Request for all score for user email {} received", email);

		final Scores scores = scoreService.getScore(email);

		try {
			return mapper.writeValueAsString(scores);
		} catch (IOException e) {
			LOGGER.error("Cannot convert scores answers to json for user {}",
					email, e);
			return "";
		}
	}

	@Override
	public String getAllAnswersAsJson(final String email,
			final List<Integer> goodAnswers) throws TException {

		LOGGER.debug(
				"Request for all answers for user {} received, good answers are {}",
				email, goodAnswers);

		final AuditAnswers auditAnswers = userService.getAuditAnswers(email,
				goodAnswers);

		try {
			return mapper.writeValueAsString(auditAnswers);
		} catch (IOException e) {
			LOGGER.error("Cannot convert audit answers to json for user {}",
					email, e);
			return "";
		}
	}

	@Override
	public String getAnswerAsJson(final String email, final int questionNumber,
			final String question, final int goodAnswer) throws TException {

		LOGGER.debug("Request for answer to question {} for user {} received",
				questionNumber, email);

		final AuditAnswer auditAnswer = userService.getAuditAnswerFor(email,
				questionNumber, question, goodAnswer);

		try {
			return mapper.writeValueAsString(auditAnswer);
		} catch (IOException e) {
			LOGGER.error("Cannot convert audit answer to json for user {}",
					email, e);
			return "";
		}
	}

	@Override
	public void destroy() throws Exception {

		if (thriftThread != null) {
			thriftThread.requestShutdown();
		}
	}

	@Override
	public ExtendedUserInfoAndScore getExtendedUserInfo(String userId) {

		LOGGER.info("Excplicitely loading user {} ", userId);

		ExtendedUserInfoAndScore extendedUserInfoAndScore;

		User user = userService.getUserFromUserId(userId);
		if (user != null) {

			extendedUserInfoAndScore = new ExtendedUserInfoAndScore(userId,
					user.getScore(), user.getEmail(), user.getFirstname(),
					user.getLastname(), true, 0);

		} else {
			extendedUserInfoAndScore = new ExtendedUserInfoAndScore();
		}
		return extendedUserInfoAndScore;
	}

	@Override
	public BeforeAndAfterScores get50BeforeAnd50After(String userId) {

		BeforeAndAfterScores beforeAndAfterScores;

		User user = userService.getUserFromUserId(userId);
		if (user != null) {
			List<User> beforeUsers = scoreService.get50Before(user);

			List<UserInfoAndScore> retBeforeUsers = new ArrayList<UserInfoAndScore>(
					beforeUsers.size());
			for (User beforeUser : beforeUsers) {
				retBeforeUsers.add(map(beforeUser));
			}

			List<User> afterUsers = scoreService.get50After(user);

			List<UserInfoAndScore> retAfterUsers = new ArrayList<UserInfoAndScore>(
					afterUsers.size());
			for (User afterUser : afterUsers) {
				retAfterUsers.add(map(afterUser));
			}

			beforeAndAfterScores = new BeforeAndAfterScores(retBeforeUsers,
					retAfterUsers);

		} else {
			beforeAndAfterScores = new BeforeAndAfterScores();
		}

		return beforeAndAfterScores;
	}

	@Override
	public void gameCreated(final int useless) throws TException {
		userService.gameCreated();
	}
	
	@Override
	public void ping(final int useless) throws TException {}

	@Override
	public List<UserInfoAndScore> getUsers(final int from, final int limit) {
		final List<UserInfoAndScore> users = new ArrayList<UserInfoAndScore>(limit);
		final List<User> gotUsers = userService.getUsers(from, limit);
		for (final User user : gotUsers) {
			final UserInfoAndScore mappedUser = map(user);
			mappedUser.setUserId(user.getUserId());
			users.add(mappedUser);
		}
		return users;
	}
}
