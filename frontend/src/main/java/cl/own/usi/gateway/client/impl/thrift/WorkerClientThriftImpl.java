package cl.own.usi.gateway.client.impl.thrift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cl.own.usi.gateway.client.ExtendedUserInfoAndScore;
import cl.own.usi.gateway.client.UserAndScore;
import cl.own.usi.gateway.client.UserAndScoreAndAnswer;
import cl.own.usi.gateway.client.BeforeAndAfterScores;
import cl.own.usi.gateway.client.UserInfoAndScore;
import cl.own.usi.gateway.client.UserLogin;
import cl.own.usi.gateway.client.WorkerClient;
import cl.own.usi.gateway.client.pool.MultiPool;
import cl.own.usi.gateway.client.pool.ObjectPoolFactory;
import cl.own.usi.gateway.client.pool.Pool;
import cl.own.usi.gateway.client.pool.exception.FactoryException;
import cl.own.usi.gateway.client.pool.impl.MultiPoolImpl;
import cl.own.usi.gateway.client.pool.impl.PoolImpl;
import cl.own.usi.model.Game;
import cl.own.usi.model.Question;
import cl.own.usi.service.GameService;
import cl.own.usi.thrift.WorkerRPC.Client;

/**
 * Thrift client.
 *
 * @author bperroud
 *
 */
@Component
public class WorkerClientThriftImpl implements WorkerClient, InitializingBean, DisposableBean {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WorkerClientThriftImpl.class);

	private static final int USELESS_INT = 123456;

	@Autowired
	private GameService gameService;

	private final MultiPool<WorkerHost, Client> pools = new ThriftMultiPool();

	@Value(value = "${frontend.backendConnections:10}")
	private int backendConnections;
	
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	
	@Override
	public UserAndScore validateUserAndInsertQuestionRequest(
			final String userId, final int questionNumber) {

		return new ThriftAction<UserAndScore>(pools) {

			@Override
			protected UserAndScore action(final Client client)
					throws TException {
				final cl.own.usi.thrift.UserAndScore userAndScore = client
						.validateUserAndInsertQuestionRequest(userId,
								questionNumber);
				return map(userAndScore);
			}

			@Override
			protected String getActionDescription() {
				return String.format(
						"validateUserAndInsertQuestionRequest(%s, %d)", userId,
						questionNumber);
			}
		}.doAction();
	}

	@Override
	public UserAndScoreAndAnswer validateUserAndInsertQuestionResponseAndUpdateScore(
			final String userId, final int questionNumber, final Integer answer) {

		return new ThriftAction<UserAndScoreAndAnswer>(pools) {

			@Override
			protected UserAndScoreAndAnswer action(final Client client)
					throws TException {
				boolean answerCorrect = gameService.isAnswerCorrect(
						questionNumber, answer);
				Question question = gameService.getQuestion(questionNumber);
				cl.own.usi.thrift.UserAndScore userAndScore = client
						.validateUserAndInsertQuestionResponseAndUpdateScore(
								userId, questionNumber, question.getValue(),
								answer, answerCorrect);
				return map(userAndScore, answerCorrect);
			}

			@Override
			protected String getActionDescription() {
				return String
						.format("validateUserAndResponseAndScore...(%s, %d, %d)",
								userId, questionNumber, answer);
			}
		}.doAction();
	}

	@Override
	public UserAndScore validateUserAndGetScore(final String userId) {

		return new ThriftAction<UserAndScore>(pools) {

			@Override
			protected UserAndScore action(final Client client)
					throws TException {
				cl.own.usi.thrift.UserAndScore userAndScore = client
						.validateUserAndGetScore(userId);
				return map(userAndScore);
			}

			@Override
			protected String getActionDescription() {
				return String.format("validateUserAndGetScore(%s)", userId);
			}
		}.doAction();

	}

	@Override
	public UserLogin loginUser(final String email, final String password) {

		return new ThriftAction<UserLogin>(pools) {

			@Override
			protected UserLogin action(final Client client) throws TException {
				cl.own.usi.thrift.UserLogin userLogin = client.loginUser(email,
						password);

				return map(userLogin);
			}

			@Override
			protected String getActionDescription() {
				return String.format("login(%s, %s)", email, password);
			}
		}.doAction();

	}

	@Override
	public boolean insertUser(final String email, final String password,
			final String firstname, final String lastname) {

		final Boolean isInserted = new ThriftAction<Boolean>(pools) {

			@Override
			protected Boolean action(final Client client) throws TException {
				if (client.insertUser(email, password, firstname, lastname)) {
					return true;
				} else {
					LOGGER.warn("Insertion failed, for user {}.", email);
					return false;
				}
			}

			@Override
			protected String getActionDescription() {
				return String.format("insertUser(%s, %s, %s, %s)", email,
						password, firstname, lastname);
			}
		}.doAction();

		return isInserted != null ? isInserted.booleanValue() : false;

	}

	@Override
	public void flushUsers() {

		new ThriftAction<Boolean>(pools) {

			@Override
			protected Boolean action(final Client client) throws TException {
				client.flushUsers(USELESS_INT);
				return Boolean.TRUE;
			}

			@Override
			protected String getActionDescription() {
				return "flushUsers()";
			}
		}.doAction();
	}

	@Override
	public List<UserInfoAndScore> getTop100() {

		return new ThriftAction<List<UserInfoAndScore>>(pools) {

			@Override
			protected List<UserInfoAndScore> action(final Client client)
					throws TException {
				final List<cl.own.usi.thrift.UserInfoAndScore> users = client
						.getTop100(USELESS_INT);

				int size = users == null ? 0 : users.size();

				final List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(
						size);

				if (size > 0) {
					for (cl.own.usi.thrift.UserInfoAndScore user : users) {
						retUsers.add(map(user));
					}
				}
				return retUsers;
			}

			@Override
			protected String getActionDescription() {
				return "getTop100";
			}
		}.doAction();
	}

	@Override
	public List<UserInfoAndScore> get50Before(final String userId) {

		return new ThriftAction<List<UserInfoAndScore>>(pools) {

			@Override
			protected List<UserInfoAndScore> action(Client client)
					throws TException {
				final List<cl.own.usi.thrift.UserInfoAndScore> users = client
						.get50Before(userId);

				final List<UserInfoAndScore> retUsers = map(users);

				return retUsers;
			}

			@Override
			protected String getActionDescription() {
				return String.format("get50Before(%s)", userId);
			}
		}.doAction();
	}

	@Override
	public List<UserInfoAndScore> get50After(final String userId) {

		return new ThriftAction<List<UserInfoAndScore>>(pools) {

			@Override
			protected List<UserInfoAndScore> action(Client client)
					throws TException {
				final List<cl.own.usi.thrift.UserInfoAndScore> users = client
						.get50After(userId);

				final List<UserInfoAndScore> retUsers = map(users);

				return retUsers;
			}

			@Override
			protected String getActionDescription() {
				return String.format("get50After(%s)", userId);
			}
		}.doAction();
	}

	@Override
	public String getScoreAsJson(final String email) {
		return new ThriftAction<String>(pools) {

			@Override
			protected String action(final Client client) throws TException {
				return client.getScoreAsJson(email);
			}

			@Override
			protected String getActionDescription() {
				return String.format("getScoreAsJson(%s)", email);
			}
		}.doAction();

	}

	@Override
	public String getAnswersAsJson(final String email,
			final Integer questionNumber, final Game game) {

		assert email != null : "the 'email' parameter cannot be null";
		assert questionNumber != null : "the 'questionNumber' parameter cannot be null";
		assert game != null : "the 'game' parameter cannot be null";

		// TODO: sort questions here?
		final List<Question> questions = game.getQuestions();

		return new ThriftAction<String>(pools) {

			@Override
			protected String action(final Client client) throws TException {
				if (questionNumber == null) {

					final List<Integer> goodAnswers = new ArrayList<Integer>(
							questions.size());

					for (Question question : questions) {
						goodAnswers.add(question.getCorrectChoiceNumber());
					}

					return client.getAllAnswersAsJson(email, goodAnswers);

				} else {
					String questionString = null;
					int goodAnswer = -1;

					for (Question question : questions) {
						if (questionNumber.equals(question.getNumber())) {
							questionString = question.getLabel();
							goodAnswer = question.getCorrectChoiceNumber();
						}
					}

					if (questionString == null) {
						return null;
					}

					return client.getAnswerAsJson(email,
							questionNumber.intValue(), questionString,
							goodAnswer);
				}
			}

			@Override
			protected String getActionDescription() {
				return String.format("getAnswersAsJson(%s, %d, %s)", email,
						questionNumber, game);
			}
		}.doAction();
	}

	@Override
	public void gameEnded() {

		new ThriftAction<Boolean>(pools) {

			@Override
			protected Boolean action(Client client) throws TException {
				client.gameEnded(USELESS_INT);
				return Boolean.TRUE;
			}

			@Override
			protected String getActionDescription() {
				return "startRankingsComputation()";
			}
		}.doAction();
	}

	private UserAndScore map(final cl.own.usi.thrift.UserAndScore userAndScore) {
		if (userAndScore == null) {
			return null;
		} else {
			final UserAndScore newUserAndScore = new UserAndScore(
					userAndScore.userId, userAndScore.score);
			return newUserAndScore;
		}
	}

	private ExtendedUserInfoAndScore map(
			final cl.own.usi.thrift.ExtendedUserInfoAndScore extendedUserInfoAndScore) {
		if (extendedUserInfoAndScore == null) {
			return null;
		} else {
			final ExtendedUserInfoAndScore newExtendedUserInfoAndScore = new ExtendedUserInfoAndScore(
					extendedUserInfoAndScore.userId,
					extendedUserInfoAndScore.score,
					extendedUserInfoAndScore.email,
					extendedUserInfoAndScore.firstname,
					extendedUserInfoAndScore.lastname,
					extendedUserInfoAndScore.isLogged,
					extendedUserInfoAndScore.lastQuestionAnwered);
			return newExtendedUserInfoAndScore;
		}
	}

	private UserAndScoreAndAnswer map(
			final cl.own.usi.thrift.UserAndScore userAndScore,
			final boolean answer) {
		if (userAndScore == null) {
			return null;
		} else {
			final UserAndScoreAndAnswer newUserAndScoreAndAnswer = new UserAndScoreAndAnswer(
					userAndScore.userId, userAndScore.score, answer);
			return newUserAndScoreAndAnswer;
		}
	}

	private UserInfoAndScore map(
			final cl.own.usi.thrift.UserInfoAndScore userInfoAndScore) {
		if (userInfoAndScore == null) {
			return null;
		} else {
			final UserInfoAndScore newUserInfoAndScore = new UserInfoAndScore(
					userInfoAndScore.userId, userInfoAndScore.score,
					userInfoAndScore.email, userInfoAndScore.firstname,
					userInfoAndScore.lastname);
			return newUserInfoAndScore;
		}
	}

	private List<UserInfoAndScore> map(
			final List<cl.own.usi.thrift.UserInfoAndScore> userInfoAndScores) {

		final List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(
				userInfoAndScores.size());
		for (cl.own.usi.thrift.UserInfoAndScore user : userInfoAndScores) {
			retUsers.add(map(user));
		}
		return retUsers;

	}

	private BeforeAndAfterScores map(
			final cl.own.usi.thrift.BeforeAndAfterScores beforeAndAfterScores) {
		if (beforeAndAfterScores == null) {
			return null;
		} else {
			final BeforeAndAfterScores newBeforeAndAfterScores = new BeforeAndAfterScores(
					map(beforeAndAfterScores.beforeUsers),
					map(beforeAndAfterScores.afterUsers));
			return newBeforeAndAfterScores;
		}
	}

	private UserLogin map(final cl.own.usi.thrift.UserLogin userLogin) {
		if (userLogin == null) {
			return null;
		} else {
			final UserLogin newUserLogin = new UserLogin(userLogin.userId,
					userLogin.alreadyLogged);
			return newUserLogin;
		}
	}

	public boolean addWorkerNode(final String host, final int port) {
		final WorkerHost wh = WorkerHost.create(host, port);
		return pools.addKey(wh);
	}

	private static class ThriftClientFactory implements
			ObjectPoolFactory<Client> {

		private final WorkerHost workerHost;
		private final Map<Client, TTransport> transports = new ConcurrentHashMap<Client, TTransport>();

		public ThriftClientFactory(final WorkerHost workerHost) {
			this.workerHost = workerHost;
		}

		@Override
		public Client create() throws FactoryException {

			long starttime = System.currentTimeMillis();
			TTransport transport;
			try {
				transport = new TSocket(workerHost.getHost(),
						workerHost.getPort());
				final TProtocol protocol = new TBinaryProtocol(transport);
				final Client client = new Client(protocol);
				transport.open();
				transports.put(client, transport);
				return client;
			} catch (TTransportException e) {
				throw new FactoryException(e);
			} finally {
				long creationtime = System.currentTimeMillis() - starttime;
				if (creationtime > 20L) {
					LOGGER.warn(
						"ThriftClientFactory create new connection to {} in {} ms",
						workerHost.getHost(), creationtime);
				}
			}
		}

		@Override
		public boolean validate(final Client object) throws FactoryException {
			TTransport transport = transports.get(object);
			if (transport != null) {
				return transport.isOpen();
			} else {
				return false;
			}
		}

		@Override
		public void destroy(final Client object) {

			TTransport transport = transports.remove(object);
			if (transport != null) {
				transport.close();
			}

		}

	}

	private class ThriftMultiPool extends MultiPoolImpl<WorkerHost, Client> {

		private final Random random = new Random();
		private final static int ZERO = 0;

		private final ThreadLocal<Integer> counter = new ThreadLocal<Integer>();

		public ThriftMultiPool() {
		}

		@Override
		protected Pool<Client> createPool(final WorkerHost key) {
			Pool<Client> pool = new PoolImpl<Client>(backendConnections);
			ObjectPoolFactory<Client> factory = new ThriftClientFactory(key);
			pool.setFactory(factory);
			return pool;
		}

		protected WorkerHost getKey() {
			if (keys.isEmpty()) {
				return null;
			} else {
				Integer value = counter.get();
				if (value == null) {
					value = random.nextInt(keys.size());
				} else {
					value += 1;
				}
				if (value >= keys.size()) {
					value = ZERO;
				}
				try {
					return keys.get(value);
				} catch (IndexOutOfBoundsException e) {
					value -= 1;
					return null;
				} finally {
					counter.set(value);
				}
			}
		}

		public List<WorkerHost> getKeys() {
			return Collections.unmodifiableList(keys);
		}
		
	}

	static final class WorkerHost {
		private final String host;
		private final int port;
		private static final ConcurrentMap<String, WorkerHost> workerHosts = new ConcurrentHashMap<String, WorkerHost>();

		public static WorkerHost create(final String host, final int port) {
			String key = key(host, port);

			if (!workerHosts.containsKey(key)) {
				WorkerHost wh = new WorkerHost(host, port);
				WorkerHost tmpWh = workerHosts.putIfAbsent(key, wh);
				if (tmpWh == null) {
					return wh;
				} else {
					return tmpWh;
				}
			} else {
				return workerHosts.get(key);
			}
		}

		private static String key(String host, int port) {
			return host + port;
		}

		private WorkerHost(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

	}

	@Override
	public BeforeAndAfterScores get50BeforeAnd50After(final String userId) {

		return new ThriftAction<BeforeAndAfterScores>(pools) {

			@Override
			protected BeforeAndAfterScores action(final Client client)
					throws TException {
				final cl.own.usi.thrift.BeforeAndAfterScores beforeAndAfterScores = client
						.get50BeforeAnd50After(userId);
				return map(beforeAndAfterScores);
			}

			@Override
			protected String getActionDescription() {
				return String.format("get50BeforeAnd50After(%s)", userId);
			}
		}.doAction();

	}

	@Override
	public ExtendedUserInfoAndScore getExtendedUserInfo(final String userId) {

		return new ThriftAction<ExtendedUserInfoAndScore>(pools) {

			@Override
			protected ExtendedUserInfoAndScore action(final Client client)
					throws TException {
				final cl.own.usi.thrift.ExtendedUserInfoAndScore beforeAndAfterScores = client
						.getExtendedUserInfo(userId);
				return map(beforeAndAfterScores);
			}

			@Override
			protected String getActionDescription() {
				return String.format("getExtendedUserInfo(%s)", userId);
			}
		}.doAction();

	}

	@Override
	public void gameCreated() {

		new ThriftAction<Integer>(pools) {

			@Override
			protected Integer action(final Client client) throws TException {
				client.gameCreated(USELESS_INT);
				return USELESS_INT;
			}

			@Override
			protected String getActionDescription() {
				return String.format("gameCreated()");
			}
		}.doAction();
	}
	
	@Override
	public void ping() {
		new ThriftAction<Integer>(pools) {

			@Override
			protected Integer action(final Client client) throws TException {
				client.ping(USELESS_INT);
				return USELESS_INT;
			}

			@Override
			protected String getActionDescription() {
				return String.format("ping()");
			}
		}.doAction();
	}
	
	private class ThriftPoolTestWorker implements Runnable {

		private final ThriftMultiPool multiPool;
		
		public ThriftPoolTestWorker(ThriftMultiPool multiPool) {
			this.multiPool = multiPool;
		}
		
		@Override
		public void run() {
			List<WorkerHost> keys = multiPool.getKeys();
			int numIterations = keys.size() * 2;
			for (int i = 0; i < numIterations; i++) {
				ping();
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		scheduledExecutorService.scheduleWithFixedDelay(new ThriftPoolTestWorker((ThriftMultiPool)pools), 30, 10, TimeUnit.SECONDS);
	}

	@Override
	public void destroy() throws Exception {
		scheduledExecutorService.shutdown();
		scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS);
	}

	@Override
	public List<UserInfoAndScore> getUsers(final int from, final int limit) {
		
		return new ThriftAction<List<UserInfoAndScore>>(pools) {

			@Override
			protected List<UserInfoAndScore> action(Client client)
					throws TException {
				final List<cl.own.usi.thrift.UserInfoAndScore> users = client
						.getUsers(from, limit);

				final List<UserInfoAndScore> retUsers = map(users);

				return retUsers;
			}

			@Override
			protected String getActionDescription() {
				return String.format("getUsers(%d, %d)", from, limit);
			}
		}.doAction();
		
	}
}
