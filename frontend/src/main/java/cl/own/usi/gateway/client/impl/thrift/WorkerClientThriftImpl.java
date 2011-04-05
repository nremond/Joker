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
import org.springframework.beans.factory.annotation.Autowired;
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
public class WorkerClientThriftImpl implements WorkerClient {

	@Autowired
	private GameService gameService;

	private MultiPool<WorkerHost, Client> pools = new ThriftMultiPool();

	@Override
	public UserAndScore validateUserAndInsertQuestionRequest(
			final String userId, final int questionNumber) {

		return new ThriftAction<UserAndScore>(pools) {

			@Override
			protected UserAndScore action(Client client) throws TException {
				final cl.own.usi.thrift.UserAndScore userAndScore = client
						.validateUserAndInsertQuestionRequest(userId,
								questionNumber);
				return map(userAndScore);
			}
		}.doAction();
	}

	@Override
	public UserAndScoreAndAnswer validateUserAndInsertQuestionResponseAndUpdateScore(
			final String userId, final int questionNumber, final Integer answer) {

		return new ThriftAction<UserAndScoreAndAnswer>(pools) {

			@Override
			protected UserAndScoreAndAnswer action(Client client)
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
		}.doAction();
	}

	@Override
	public UserAndScore validateUserAndGetScore(final String userId) {

		return new ThriftAction<UserAndScore>(pools) {

			@Override
			protected UserAndScore action(Client client) throws TException {
				cl.own.usi.thrift.UserAndScore userAndScore = client
						.validateUserAndGetScore(userId);
				return map(userAndScore);
			}
		}.doAction();

	}

	@Override
	public UserLogin loginUser(final String email, final String password) {

		return new ThriftAction<UserLogin>(pools) {

			@Override
			protected UserLogin action(Client client) throws TException {
				cl.own.usi.thrift.UserLogin userLogin = client.loginUser(email,
						password);

				return map(userLogin);
			}
		}.doAction();

	}

	@Override
	public boolean insertUser(final String email, final String password,
			final String firstname, final String lastname) {

		final Boolean isInserted = new ThriftAction<Boolean>(pools) {

			@Override
			protected Boolean action(Client client) throws TException {
				return client.insertUser(email, password, firstname, lastname);
			}
		}.doAction();

		return isInserted != null ? isInserted.booleanValue() : false;

	}

	@Override
	public void flushUsers() {

		new ThriftAction<Boolean>(pools) {

			@Override
			protected Boolean action(Client client) throws TException {
				client.flushUsers();
				return Boolean.TRUE;
			}
		}.doAction();
	}

	@Override
	public List<UserInfoAndScore> getTop100() {

		return new ThriftAction<List<UserInfoAndScore>>(pools) {

			@Override
			protected List<UserInfoAndScore> action(Client client)
					throws TException {
				final List<cl.own.usi.thrift.UserInfoAndScore> users = client
						.getTop100();

				final List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(
						users.size());
				for (cl.own.usi.thrift.UserInfoAndScore user : users) {
					retUsers.add(map(user));
				}
				return retUsers;
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
						goodAnswers.add(question.getCorrectChoice());
					}

					return client.getAllAnswersAsJson(email, goodAnswers);

				} else {
					String questionString = null;
					int goodAnswer = -1;

					for (Question question : questions) {
						if (questionNumber.equals(question.getNumber())) {
							questionString = question.getLabel();
							goodAnswer = question.getCorrectChoice();
						}
					}

					if (questionString == null) {
						// TODO
					}

					return client.getAnswerAsJson(email,
							questionNumber.intValue(), questionString,
							goodAnswer);
				}
			}
		}.doAction();
	}

	@Override
	public void startRankingsComputation() {

		new ThriftAction<Boolean>(pools) {

			@Override
			protected Boolean action(Client client) throws TException {
				client.startRankingsComputation();
				return Boolean.TRUE;
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

	static class ThriftMultiPool extends MultiPoolImpl<WorkerHost, Client> {

		private final Random r = new Random();

		@Override
		protected Pool<Client> createPool(WorkerHost key) {
			Pool<Client> pool = new PoolImpl<Client>();
			ObjectPoolFactory<Client> factory = new ThriftClientFactory(key);
			pool.setFactory(factory);
			return pool;
		}

		protected WorkerHost getKey() {
			if (keys.isEmpty()) {
				return null;
			} else {
				return keys.get(r.nextInt(keys.size()));
			}
		}

	}

	static final class WorkerHost {
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
			protected BeforeAndAfterScores action(Client client)
					throws TException {
				final cl.own.usi.thrift.BeforeAndAfterScores beforeAndAfterScores = client
						.get50BeforeAnd50After(userId);
				return map(beforeAndAfterScores);
			}
		}.doAction();

	}

	@Override
	public ExtendedUserInfoAndScore getExtendedUserInfo(final String userId) {

		return new ThriftAction<ExtendedUserInfoAndScore>(pools) {

			@Override
			protected ExtendedUserInfoAndScore action(Client client)
					throws TException {
				final cl.own.usi.thrift.ExtendedUserInfoAndScore beforeAndAfterScores = client
						.getExtendedUserInfo(userId);
				return map(beforeAndAfterScores);
			}
		}.doAction();

	}

}
