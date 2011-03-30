package cl.own.usi.gateway.client.impl.thrift;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.gateway.client.UserAndScore;
import cl.own.usi.gateway.client.UserAndScoreAndAnswer;
import cl.own.usi.gateway.client.UserInfoAndScore;
import cl.own.usi.gateway.client.WorkerClient;
import cl.own.usi.gateway.client.pool.MultiPool;
import cl.own.usi.gateway.client.pool.ObjectPoolFactory;
import cl.own.usi.gateway.client.pool.Pool;
import cl.own.usi.gateway.client.pool.exception.FactoryException;
import cl.own.usi.gateway.client.pool.exception.PoolException;
import cl.own.usi.gateway.client.pool.impl.MultiPoolImpl;
import cl.own.usi.gateway.client.pool.impl.PoolImpl;
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
public class WorkerClientThriftImpl implements WorkerClient {

	private static final int THRIFT_RETRY = 3;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WorkerClientThriftImpl.class);

	@Autowired
	private GameService gameService;

	private MultiPool<WorkerHost, Client> pools = new ThriftMultiPool();

	@Override
	public UserAndScore validateUserAndInsertQuestionRequest(
			final String userId, final int questionNumber) {

		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				final cl.own.usi.thrift.UserAndScore userAndScore = client
						.validateUserAndInsertQuestionRequest(userId,
								questionNumber);
				return map(userAndScore);
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
		return null;
	}

	@Override
	public UserAndScoreAndAnswer validateUserAndInsertQuestionResponseAndUpdateScore(
			final String userId, final int questionNumber, final Integer answer) {

		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				boolean answerCorrect = gameService.isAnswerCorrect(
						questionNumber, answer);
				Question question = gameService.getQuestion(questionNumber);
				cl.own.usi.thrift.UserAndScore userAndScore = client
						.validateUserAndInsertQuestionResponseAndUpdateScore(
								userId, questionNumber, question.getValue(),
								answer, answerCorrect);
				return map(userAndScore, answerCorrect);
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
		return null;
	}

	@Override
	public UserAndScore validateUserAndGetScore(final String userId) {

		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				cl.own.usi.thrift.UserAndScore userAndScore = client
						.validateUserAndGetScore(userId);
				return map(userAndScore);
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
		return null;
	}

	@Override
	public String loginUser(final String email, final String password) {

		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				return client.loginUser(email, password);
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
		return null;
	}

	@Override
	public boolean insertUser(final String email, final String password,
			final String firstname, final String lastname) {

		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				return client.insertUser(email, password, firstname, lastname);
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
		return false;
	}

	@Override
	public void flushUsers() {

		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				client.flushUsers();
				return;
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
	}

	@Override
	public List<UserInfoAndScore> getTop100() {

		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				List<cl.own.usi.thrift.UserInfoAndScore> users = client
						.getTop100();

				List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(
						users.size());
				for (cl.own.usi.thrift.UserInfoAndScore user : users) {
					retUsers.add(map(user));
				}
				return retUsers;
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
		return null;
	}

	@Override
	public List<UserInfoAndScore> get50Before(final String userId) {

		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				final List<cl.own.usi.thrift.UserInfoAndScore> users = client
						.get50Before(userId);

				final List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(
						users.size());
				for (cl.own.usi.thrift.UserInfoAndScore user : users) {
					retUsers.add(map(user));
				}
				return retUsers;
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
		return null;
	}

	@Override
	public List<UserInfoAndScore> get50After(final String userId) {

		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				final List<cl.own.usi.thrift.UserInfoAndScore> users = client
						.get50After(userId);

				final List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(
						users.size());
				for (cl.own.usi.thrift.UserInfoAndScore user : users) {
					retUsers.add(map(user));
				}
				return retUsers;
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
		return null;
	}

	@Override
	public String getAnswersAsJson(final String email,
			final Integer questionNumber) {

		final ThriftRetryableAction<String> action = new GetAnswersAsJsonAction(
				email, questionNumber);

		return executeAction(action, String.class);
	}

	private <T> T executeAction(final ThriftRetryableAction<T> action,
			Class<T> resultClass) {
		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				return action.doAction(client);
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
		return null;

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

	private UserAndScoreAndAnswer map(
			cl.own.usi.thrift.UserAndScore userAndScore, boolean answer) {
		if (userAndScore == null) {
			return null;
		} else {
			final UserAndScoreAndAnswer newUserAndScoreAndAnswer = new UserAndScoreAndAnswer(
					userAndScore.userId, userAndScore.score, answer);
			return newUserAndScoreAndAnswer;
		}
	}

	private UserInfoAndScore map(
			cl.own.usi.thrift.UserInfoAndScore userInfoAndScore) {
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

	public boolean addWorkerNode(final String host, final int port) {
		final WorkerHost wh = WorkerHost.create(host, port);
		return pools.addKey(wh);
	}

	private Client getClient() {
		try {
			return pools.borrow();
		} catch (PoolException e) {
			throw new IllegalStateException("No pool borrowed...", e);
		}
	}

	private void release(final Client client) {
		try {
			if (client != null) {
				pools.release(client);
			}
		} catch (PoolException e) {
			LOGGER.error(
					"Something bad happened while trying to release client {}!",
					client, e);
		}
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

			TTransport transport;
			try {
				transport = new TSocket(workerHost.getHost(),
						workerHost.getPort());
				TProtocol protocol = new TBinaryProtocol(transport);
				Client client = new Client(protocol);
				transport.open();
				transports.put(client, transport);
				return client;
			} catch (TTransportException e) {
				throw new FactoryException(e);
			}
		}

		@Override
		public boolean validate(Client object) throws FactoryException {
			TTransport transport = transports.get(object);
			if (transport != null) {
				return transport.isOpen();
			} else {
				return false;
			}
		}

		@Override
		public void destroy(Client object) {

			TTransport transport = transports.remove(object);
			if (transport != null) {
				transport.close();
			}

		}

	}

	private static class ThriftMultiPool extends
			MultiPoolImpl<WorkerHost, Client> {

		@Override
		protected Pool<Client> createPool(WorkerHost key) {
			Pool<Client> pool = new PoolImpl<Client>();
			ObjectPoolFactory<Client> factory = new ThriftClientFactory(key);
			pool.setFactory(factory);
			return pool;
		}

		Random r = new Random();

		protected WorkerHost getKey() {
			if (keys.isEmpty()) {
				return null;
			} else {
				return keys.get(r.nextInt(keys.size()));
			}
		}

	}

	private static class WorkerHost {
		private final String host;
		private final int port;
		private static ConcurrentMap<String, WorkerHost> workerHosts = new ConcurrentHashMap<String, WorkerHost>();

		public static WorkerHost create(String host, int port) {
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
			return host + String.valueOf(port);
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
	public void startRankingsComputation() {

		Client client = getClient();
		try {
			client.startRankingsComputation();
		} catch (TException e) {
			pools.invalidate(client);
		} finally {
			release(client);
		}
	}
}
