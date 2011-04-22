package cl.own.usi.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cl.own.usi.dao.GameDAO;
import cl.own.usi.gateway.client.UserInfoAndScore;
import cl.own.usi.gateway.client.WorkerClient;
import cl.own.usi.gateway.netty.QuestionWorker;
import cl.own.usi.gateway.utils.ExecutorUtil;
import cl.own.usi.gateway.utils.ScoresHelper;
import cl.own.usi.gateway.utils.Twitter;
import cl.own.usi.model.Game;
import cl.own.usi.model.Question;
import cl.own.usi.service.CachedScoreService;
import cl.own.usi.service.GameService;

/**
 * Game service implementation.
 * 
 * @author bperroud
 * 
 */
@Service
public class GameServiceImpl implements GameService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GameServiceImpl.class);

	@Autowired
	private ExecutorUtil executorUtil;

	@Autowired
	private GameDAO gameDAO;

	@Autowired
	private Twitter twitter;

	@Autowired
	private WorkerClient workerClient;
	
	@Autowired
	private CachedScoreService scoreService;

	private final ExecutorService executorService = Executors
			.newSingleThreadExecutor();

	private GameSynchronization gameSynchronization;

	private static final int FIRST_QUESTION = 1;

	private static final String TWITTER_MESSAGE = "Notre Appli supporte %d joueurs #challengeUSI2011";

	private boolean twitt = false;

	private final AtomicReference<String> top100AsString = new AtomicReference<String>();

	private final AtomicBoolean gameRunning = new AtomicBoolean(false);

	@Value(value = "${frontend.twitt:false}")
	public void setTwitt(boolean twitt) {
		this.twitt = twitt;
	}

	public boolean insertGame(int usersLimit, int questionTimeLimit,
			int pollingTimeLimit, int synchroTimeLimit,
			List<Map<String, Map<String, Boolean>>> questions) {

		if (!gameRunning.compareAndSet(false, true)) {
			return false;
		}

		resetPreviousGame();

		Game game = gameDAO.insertGame(usersLimit, questionTimeLimit,
				pollingTimeLimit, synchroTimeLimit, mapToQuestion(questions));

		gameSynchronization = new GameSynchronization(game);

		executorService.execute(new GameFlowWorker(gameSynchronization));

		return true;
	}

	@Override
	public Game getGame() {
		return gameDAO.getGame();
	}

	private void resetPreviousGame() {
		GameSynchronization oldGameSynchronization = gameSynchronization;
		if (oldGameSynchronization != null) {
			oldGameSynchronization.currentQuestionToAnswer = 0;
			oldGameSynchronization.reseted = true;
			for (Map.Entry<Question, QuestionSynchronization> entry : oldGameSynchronization.questionSynchronizations
					.entrySet()) {
				QuestionSynchronization questionSynchronization = entry
						.getValue();
				questionSynchronization.questionReadyLatch.countDown();
			}

			for (int i = 0; i < oldGameSynchronization.game.getUsersLimit(); i++) {
				oldGameSynchronization.allUsersRankingLatch.countDown();
			}
		}
	}

	private List<Question> mapToQuestion(
			List<Map<String, Map<String, Boolean>>> questions) {
		List<Question> list = new ArrayList<Question>();
		int number = FIRST_QUESTION;
		for (Map<String, Map<String, Boolean>> element : questions) {
			for (Map.Entry<String, Map<String, Boolean>> entry : element
					.entrySet()) {
				Question question = new Question();
				question.setNumber(number);
				question.setLabel(entry.getKey());
				question.setChoices(new ArrayList<String>(entry.getValue()
						.size()));
				int value = 1;
				if (number > 5 && number <= 10) {
					value = 5;
				} else if (number > 10 && number <= 15) {
					value = 10;
				} else if (number > 15) {
					value = 15;
				}
				question.setValue(value);
				int i = 1;
				for (Map.Entry<String, Boolean> answer : entry.getValue()
						.entrySet()) {
					question.getChoices().add(answer.getKey());
					if (answer.getValue()) {
						question.setCorrectChoice(i);
					}
					i++;
				}
				list.add(question);
				number++;
			}
		}
		return list;
	}

	@Override
	public Question getQuestion(int questionNumber) {
		return gameDAO.getQuestion(questionNumber);
	}

	private QuestionSynchronization getQuestionSync(int questionNumber) {
		return gameSynchronization.getQuestionSynchronization(questionNumber);
	}

	@Override
	public boolean waitOtherUsers(int questionNumber)
			throws InterruptedException {
		QuestionSynchronization questionSync = getQuestionSync(questionNumber);
		if (questionSync == null) {
			return false;
		} else {
			int timeToWait = gameDAO.getGame().getQuestionTimeLimit()
					+ gameDAO.getGame().getSynchroTimeLimit();
			if (questionNumber == 1) {
				timeToWait = gameDAO.getGame().getPollingTimeLimit();
			}
			boolean enter = questionSync.questionReadyLatch.await(
					timeToWait + 5, TimeUnit.SECONDS);
			return enter && gameSynchronization.currentQuestionRunning;
		}
	}

	@Override
	public boolean enterGame(String userId) {
		if (gameSynchronization != null) {
			gameSynchronization.waitForFirstLogin.countDown();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void requestRanking(String userId) {
		gameSynchronization.allUsersRankingLatch.countDown();
	}

	/**
	 * Class managing the game flow. A new instance is started at each new
	 * {@link Game}.
	 * 
	 * @author bperroud
	 * 
	 */
	private class GameFlowWorker implements Runnable {

		private final GameSynchronization gameSynchronization;

		public GameFlowWorker(GameSynchronization gameSynchronization) {
			this.gameSynchronization = gameSynchronization;
		}

		@Override
		public void run() {

			try {
				LOGGER.info(
						"Start game with {} questions, {} users, {} seconds of question time frame, {} seconds of synchrotime",
						new Object[] { gameDAO.getGame().getNumberOfQuestion(),
								gameDAO.getGame().getUsersLimit(),
								gameDAO.getGame().getQuestionTimeLimit(),
								gameDAO.getGame().getSynchroTimeLimit() });

				// Wait for the first login before starting pollingtimelimit
				// timer.
				try {
					LOGGER.debug("Wait for first login");
					gameSynchronization.waitForFirstLogin.await();
					LOGGER.debug("First user have joined the game");
				} catch (InterruptedException e) {
					LOGGER.warn("Interrupted", e);
					return;
				}

				// First login recorded, wait either to enough users request the
				// first question, or pollingtimelimit.
				try {
					LOGGER.debug("Wait on all users login and requesting the first question.");
					boolean awaited = gameSynchronization.enoughUsersLatch
							.await(gameSynchronization.game
									.getPollingTimeLimit(), TimeUnit.SECONDS);
					if (awaited) {
						LOGGER.info("Enough users have joined the game and requested the first question.");
					} else {
						LOGGER.info("Waiting time is ellapsed, starting anyway.");
					}
				} catch (InterruptedException e) {
					LOGGER.warn("Interrupted", e);
					return;
				}

				// For each question, allow response and next question request,
				// wait questionwaittime and do some processing in synchrotime.
				for (int i = FIRST_QUESTION; i <= gameSynchronization.game
						.getNumberOfQuestion(); i++) {

					gameSynchronization.currentQuestionToRequest = i + 1;

					LOGGER.info(
							"Starting question {}. Response question number {}",
							i, gameSynchronization.currentQuestionToAnswer);

					QuestionSynchronization questionSynchronization = gameSynchronization
							.getQuestionSynchronization(i);

					gameSynchronization.currentQuestionRunning = true;

					// Send questions to the users
					questionSynchronization.questionReadyLatch.countDown();

					try {
						LOGGER.debug(
								"Wait to all users answer, or till the timeout {}",
								gameSynchronization.game.getQuestionTimeLimit());

						// Wait the question time limit
						LOGGER.debug("Question wait time ...");

						Thread.sleep(TimeUnit.SECONDS
								.toMillis(gameSynchronization.game
										.getQuestionTimeLimit()));

						LOGGER.debug("Question wait time ... done");

						// synchrotime processing, except for the last question
						if (i < gameSynchronization.game.getNumberOfQuestion()) {
							LOGGER.debug("Synchrotime ...");

							long starttime = System.currentTimeMillis();

							questionSynchronization.lock.lock();
							gameSynchronization.currentQuestionToAnswer++;
							gameSynchronization.currentQuestionRunning = false;
							for (Runnable r : questionSynchronization.waitingQueue) {
								executorUtil.getExecutorService().execute(r);
							}
							questionSynchronization.lock.unlock();

							for (int oldQuestionNumber = FIRST_QUESTION; oldQuestionNumber < i; oldQuestionNumber++) {
								QuestionSynchronization oldQuestionSynchronization = gameSynchronization
										.getQuestionSynchronization(oldQuestionNumber);
								for (Runnable r : oldQuestionSynchronization.waitingQueue) {
									executorUtil.getExecutorService()
											.execute(r);
								}
							}
							long stoptime = System.currentTimeMillis();

							long synchrotime = TimeUnit.SECONDS
									.toMillis(gameSynchronization.game
											.getSynchroTimeLimit())
									+ starttime - stoptime;
							if (synchrotime > 0) {
								// mmmh, weird specs...
								Thread.sleep(synchrotime);
							}

							LOGGER.debug("Synchrotime done");
						}

					} catch (InterruptedException e) {
						LOGGER.warn("Interrupted", e);
						return;
					}

					LOGGER.info(
							"Question {} finished, going to the next question",
							i);

					if (gameSynchronization.reseted) {
						break;
					}

				}

				LOGGER.info("All questions finished. Doing some processing, latest synchrotime, and request for ranking will be allowed");

				LOGGER.debug("Latest synchrotime ...");

				long starttime = System.currentTimeMillis();

				gameSynchronization.currentQuestionToAnswer++;
				gameSynchronization.currentQuestionRunning = false;

				if (!gameSynchronization.reseted) {
					workerClient.gameEnded();

					List<UserInfoAndScore> top100 = workerClient.getTop100();
					StringBuilder sb = new StringBuilder();
					ScoresHelper.appendUsersScores(top100, sb);
					top100AsString.set(sb.toString());

					int concurrentWorkers = executorUtil.getPoolSize();
					int slicePortion = (gameSynchronization.game.getUsersLimit() / concurrentWorkers) + 1;
					for (int i = 0; i < concurrentWorkers; i++) {
						executorUtil.getExecutorService().execute(new ScoreLoadingWorker(i * slicePortion, slicePortion));
					}
					
					long stoptime = System.currentTimeMillis();

					LOGGER.debug(
							"Ranking computation and top100 query done in {} ms. Returns {} UserInfoAndScores.",
							(stoptime - starttime), top100.size());

					long synchrotime = TimeUnit.SECONDS
							.toMillis(gameSynchronization.game
									.getSynchroTimeLimit())
							+ starttime - stoptime;
					if (synchrotime > 0) {
						try {
							// mmmh, weird specs again...
							Thread.sleep(synchrotime);
						} catch (InterruptedException e) {
							LOGGER.warn("Interrupted", e);
							return;
						}
					} else {
						LOGGER.warn(
								"Ranking computation exceeded synchrotime by {} ms",
								-synchrotime);
					}

					LOGGER.debug("Latest synchrotime done");

					// TODO or FIXME : if startRankingsComputation() or
					// getTop100()
					// take more than synchrotime, we can have /ranking request
					// that
					// will failed with no reason.
					gameSynchronization.rankingRequestAllowed = true;

					LOGGER.info("Ranking requests are now allowed, waiting all users have requested ranking");

					gameRunning.set(false);

					try {
						gameSynchronization.allUsersRankingLatch.await();

						if (twitt && !gameSynchronization.reseted) {
							twitter.twitt(String.format(TWITTER_MESSAGE,
									gameSynchronization.game.getUsersLimit()));
						}

					} catch (InterruptedException e) {
						LOGGER.warn("Interrupted", e);
						return;
					}
				}

			} finally {
				gameRunning.set(false);
			}
		}
	}

	@Override
	public boolean validateQuestionToAnswer(int questionNumber) {
		if (gameSynchronization == null) {
			return false;
		} else {
			return questionNumber == gameSynchronization.currentQuestionToAnswer
					&& gameSynchronization.currentQuestionRunning;
		}
	}

	@Override
	public boolean validateQuestionToRequest(int questionNumber) {
		if (gameSynchronization == null) {
			return false;
		} else {
			boolean questionValid = questionNumber == gameSynchronization.currentQuestionToRequest;
			if (questionValid
					&& gameSynchronization.currentQuestionToRequest == FIRST_QUESTION) {
				gameSynchronization.enoughUsersLatch.countDown();
			}
			return questionValid;
		}
	}

	/**
	 * Containers of all needed synchronization stuff for the current
	 * {@link Game}. A new instance is created at each new {@link Game}.
	 * 
	 * @author bperroud
	 * 
	 */
	private static class GameSynchronization {

		private final CountDownLatch waitForFirstLogin;

		private final CountDownLatch enoughUsersLatch;

		private final CountDownLatch allUsersRankingLatch;

		private final Map<Question, QuestionSynchronization> questionSynchronizations;

		private volatile int currentQuestionToRequest = 1;
		private volatile int currentQuestionToAnswer = 1;
		private volatile boolean currentQuestionRunning = false;
		private volatile boolean rankingRequestAllowed = false;

		private final Game game;
		private volatile boolean reseted = false;

		public GameSynchronization(Game game) {

			this.game = game;

			questionSynchronizations = new HashMap<Question, QuestionSynchronization>(
					game.getQuestions().size());

			for (Question question : game.getQuestions()) {
				questionSynchronizations.put(question,
						new QuestionSynchronization());
			}

			enoughUsersLatch = new CountDownLatch(game.getUsersLimit());
			waitForFirstLogin = new CountDownLatch(1);
			allUsersRankingLatch = new CountDownLatch(game.getUsersLimit());
		}

		QuestionSynchronization getQuestionSynchronization(int questionNumber) {
			return questionSynchronizations.get(game.getQuestions().get(
					questionNumber - 1));
		}

	}

	/**
	 * Containers of all needed synchronization stuff for the {@link Question}s.
	 * A new instance is created at each {@link Question} in a new {@link Game}.
	 * 
	 * @author bperroud
	 * 
	 */
	private static class QuestionSynchronization {

		private final CountDownLatch questionReadyLatch;

		private final Queue<Runnable> waitingQueue = new LinkedBlockingQueue<Runnable>();
		private final Lock lock = new ReentrantLock();

		public QuestionSynchronization() {
			questionReadyLatch = new CountDownLatch(1);
		}

	}

	public void scheduleQuestionReply(QuestionWorker questionWorker) {

		if (questionWorker.getQuestionNumber() <= gameSynchronization.currentQuestionToRequest) {
			executorUtil.getExecutorService().execute(questionWorker);
		} else {
			QuestionSynchronization questionSynchronization = getQuestionSync(questionWorker
					.getQuestionNumber());

			questionSynchronization.lock.lock();

			// double check
			if (questionWorker.getQuestionNumber() <= gameSynchronization.currentQuestionToRequest) {
				executorUtil.getExecutorService().execute(questionWorker);
			} else {
				LOGGER.debug(
						"Too early question request for question {}, putting in a temporaray queue",
						questionWorker.getQuestionNumber());
				questionSynchronization.waitingQueue.offer(questionWorker);
			}

			questionSynchronization.lock.unlock();

		}
	}

	private Integer validateAnswer(int questionNumber, Integer answer) {
		Question question = getQuestion(questionNumber);

		if (question == null || answer == null) {
			return null;
		} else {

			if (answer < 1 || answer > question.getChoices().size()) {
				return null;
			} else {
				return answer;
			}
		}
	}

	@Override
	public boolean isAnswerCorrect(final int questionNumber,
			final Integer answer) {

		final Integer validatedAnswer = validateAnswer(questionNumber, answer);

		if (validatedAnswer == null) {
			return false;
		} else {
			Question question = getQuestion(questionNumber);
			if (question == null) {
				return false;
			} else {
				return question.getCorrectChoice() == validatedAnswer;
			}
		}
	}

	@Override
	public boolean isRankingRequestAllowed() {
		return gameSynchronization != null
				&& gameSynchronization.rankingRequestAllowed;
	}

	@Override
	public String getTop100AsString() {
		return top100AsString.get();
	}

	private class ScoreLoadingWorker implements Runnable {

		private static final int BATCH_SIZE = 2000;
		
		private final int from;
		private final int limit;
		
		public ScoreLoadingWorker(final int from, final int limit) {
			this.from = from;
			this.limit = limit;
		}
		
		@Override
		public void run() {
			
			int currentFrom = from;
			int currentLimit;
			boolean nextIteration = true;
			do {
				currentLimit = Math.min(limit - currentFrom + from + 1, BATCH_SIZE);

				long starttime = System.currentTimeMillis();
				
				List<UserInfoAndScore> users = workerClient.getUsers(currentFrom, currentLimit);

				LOGGER.info("Loaded {} users from {} limit {} in {} ms", new Object[] {users.size(), currentFrom, currentLimit, System.currentTimeMillis() - starttime});

				for (UserInfoAndScore user : users) {
					scoreService.addUser(user.getUserId(), user.getLastname(), user.getFirstname(), user.getEmail(), user.getScore());
				}
				
				if (users.size() < currentLimit || currentFrom >= from + limit || currentLimit < BATCH_SIZE) {
					nextIteration = false;
				} else {
					currentFrom += currentLimit;
				}
				
				LOGGER.info("Processed {} users (loading and inserting in the sortedset) in {} ms", users.size(), System.currentTimeMillis() - starttime);
				
			} while (nextIteration);
		}
		
	}
}
