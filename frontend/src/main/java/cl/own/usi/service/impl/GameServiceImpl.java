package cl.own.usi.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.dao.GameDAO;
import cl.own.usi.gateway.netty.QuestionWorker;
import cl.own.usi.gateway.utils.ExecutorUtil;
import cl.own.usi.model.Game;
import cl.own.usi.model.Question;
import cl.own.usi.service.GameService;

@Service
public class GameServiceImpl implements GameService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GameServiceImpl.class);

	@Autowired
	private ExecutorUtil executorUtil;

	@Autowired
	private GameDAO gameDAO;

	final ExecutorService executorService = Executors.newFixedThreadPool(2);

	private GameSynchronization gameSynchronization;

	public boolean insertGame(int usersLimit, int questionTimeLimit,
			int pollingTimeLimit,
			List<Map<String, Map<String, Boolean>>> questions) {

		resetPreviousGame();

		Game game = gameDAO.insertGame(usersLimit, questionTimeLimit,
				pollingTimeLimit, mapToQuestion(questions));

		gameSynchronization = new GameSynchronization(game);

		executorService.execute(new StartOfNewQuestionWorker(
				gameSynchronization));

		return true;
	}

	private void resetPreviousGame() {
		GameSynchronization oldGameSynchronization = gameSynchronization;
		if (oldGameSynchronization != null) {
			oldGameSynchronization.currentQuestionToAnswer = 0;
			for (Map.Entry<Question, QuestionSynchronization> entry : oldGameSynchronization.questionSynchronizations
					.entrySet()) {
				QuestionSynchronization questionSynchronization = entry
						.getValue();
				questionSynchronization.questionReadyLatch.countDown();
				for (int i = 0; i < oldGameSynchronization.game.getUsersLimit(); i++) {
					questionSynchronization.allUsersAnswerLatch.countDown();
				}
				// TODO : remove thread from pool.
			}
		}
	}

	Random r = new Random();

	private List<Question> mapToQuestion(
			List<Map<String, Map<String, Boolean>>> questions) {
		List<Question> list = new ArrayList<Question>();
		int number = 1;
		for (Map<String, Map<String, Boolean>> element : questions) {
			for (Map.Entry<String, Map<String, Boolean>> entry : element
					.entrySet()) {
				Question question = new Question();
				question.setNumber(number);
				question.setLabel(entry.getKey());
				question.setChoices(new ArrayList<String>(entry.getValue()
						.size()));
				// TODO : get real question's value.
				question.setValue(r.nextInt(50));
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

	public Question getQuestion(int questionNumber) {
		return gameDAO.getQuestion(questionNumber);
	}

	private QuestionSynchronization getQuestionSync(int questionNumber) {
		return gameSynchronization.getQuestionSynchronization(questionNumber);
	}

	public boolean waitOtherUsers(int questionNumber, long alreadyWaitedMili)
			throws InterruptedException {
		QuestionSynchronization questionSync = getQuestionSync(questionNumber);
		if (questionSync == null) {
			return false;
		} else {
			long remainingTimeToWait = Math.max(gameDAO.getGame()
					.getPollingTimeLimit() - alreadyWaitedMili / 1000, 0);
			boolean enter = questionSync.questionReadyLatch.await(
					remainingTimeToWait, TimeUnit.SECONDS);
			return enter && questionSync.questionRunning;
		}
	}

	public boolean enterGame(String userId) {
		if (gameSynchronization != null) {
			gameSynchronization.enoughUsersLatch.countDown();
			return true;
		} else {
			return false;
		}
	}

	private class StartOfNewQuestionWorker implements Runnable {

		final GameSynchronization gameSynchronization;

		public StartOfNewQuestionWorker(GameSynchronization gameSynchronization) {
			this.gameSynchronization = gameSynchronization;
		}

		public void run() {

			LOGGER.debug("Start game");
			try {
				LOGGER.debug("Wait on all users");
				gameSynchronization.enoughUsersLatch.await();
				LOGGER.debug("Enough users have joined the game");
			} catch (InterruptedException e) {
				LOGGER.warn("Interrupted", e);
			}

			boolean first = true;

			for (int i = 1; i <= gameSynchronization.game.getQuestions().size(); i++) {

				LOGGER.info(
						"Starting question {}. Response question number {}", i,
						gameSynchronization.currentQuestionToAnswer);
				QuestionSynchronization questionSynchronization = gameSynchronization
						.getQuestionSynchronization(i);

				questionSynchronization.questionRunning = true;

				if (!first) {
					questionSynchronization.lock.lock();
					gameSynchronization.currentQuestionToAnswer++;
					for (Runnable r : questionSynchronization.waitingQueue) {
						LOGGER.debug("Inserting a early requester to the working queue");
						executorUtil.getExecutorService().execute(r);
					}
					questionSynchronization.lock.unlock();
				}
				first = false;

				// Send questions to the users
				questionSynchronization.questionReadyLatch.countDown();

				try {
					LOGGER.debug(
							"Wait to all users answer, or till the timeout {}",
							gameSynchronization.game.getQuestionTimeLimit());

					// Wait either all users answer the question, or the time
					// limit
					boolean reachedZero = questionSynchronization.allUsersAnswerLatch
							.await(gameSynchronization.game
									.getQuestionTimeLimit(), TimeUnit.SECONDS);
					questionSynchronization.questionRunning = false;
					if (reachedZero) {
						LOGGER.debug("All users has answered, going to the next question.");
					} else {
						LOGGER.debug("Normal completion of the game, going further.");
					}

				} catch (InterruptedException e) {
					LOGGER.warn("Interrupted", e);
				}

				LOGGER.info("Question {} finished, going to the next question",
						i);

			}

			LOGGER.info("All questions finished, tweet and clean everything");
			// TODO : Tweet.
		}

	}

	protected Game getGame() {
		return gameDAO.getGame();
	}

	public boolean validateQuestionToAnswer(int questionNumber) {
		if (gameSynchronization == null) {
			return false;
		} else {
			return questionNumber == gameSynchronization.currentQuestionToAnswer;
		}
	}

	@Override
	public boolean validateQuestionToRequest(int questionNumber) {
		if (gameSynchronization == null) {
			return false;
		} else {
			return questionNumber > 0
					&& questionNumber <= gameSynchronization.game
							.getQuestions().size();
		}
	}

	private class GameSynchronization {

		private final CountDownLatch enoughUsersLatch;

		private final Map<Question, QuestionSynchronization> questionSynchronizations;

		volatile int currentQuestionToAnswer = 1;

		private final Game game;

		public GameSynchronization(Game game) {

			this.game = game;

			questionSynchronizations = new HashMap<Question, QuestionSynchronization>(
					game.getQuestions().size());

			for (Question question : game.getQuestions()) {
				questionSynchronizations.put(question,
						new QuestionSynchronization(game.getUsersLimit()));
			}

			enoughUsersLatch = new CountDownLatch(game.getUsersLimit());
		}

		// QuestionSynchronization getCurrentQuestionSynchronization() {
		// return
		// questionSynchronizations.get(game.getQuestions().get(currentQuestionToAnswer
		// - 1));
		// }

		QuestionSynchronization getQuestionSynchronization(int questionNumber) {
			return questionSynchronizations.get(game.getQuestions().get(
					questionNumber - 1));
		}
	}

	private static class QuestionSynchronization {

		private volatile boolean questionRunning = false;

		private final CountDownLatch questionReadyLatch;
		private final CountDownLatch allUsersAnswerLatch;

		private final Queue<Runnable> waitingQueue = new LinkedBlockingQueue<Runnable>();
		private final Lock lock = new ReentrantLock();

		public QuestionSynchronization(int userLimit) {
			questionReadyLatch = new CountDownLatch(1);
			allUsersAnswerLatch = new CountDownLatch(userLimit);
		}

	}

	@Override
	public boolean userAnswer(int questionNumber) {

		QuestionSynchronization questionSynchronization = getQuestionSync(questionNumber);

		if (questionSynchronization != null) {
			questionSynchronization.allUsersAnswerLatch.countDown();
			return true;
		} else {
			return false;
		}
	}

	public void scheduleQuestionReply(QuestionWorker questionWorker) {

		if (questionWorker.getQuestionNumber() <= gameSynchronization.currentQuestionToAnswer) {
			executorUtil.getExecutorService().execute(questionWorker);
		} else {
			QuestionSynchronization questionSynchronization = getQuestionSync(questionWorker
					.getQuestionNumber());

			questionSynchronization.lock.lock();

			// double check
			if (questionWorker.getQuestionNumber() <= gameSynchronization.currentQuestionToAnswer) {
				executorUtil.getExecutorService().execute(questionWorker);
			} else {
				LOGGER.info(
						"Too early question request for question {}, putting in a temporaray queue",
						questionWorker.getQuestionNumber());
				questionSynchronization.waitingQueue.offer(questionWorker);
			}

			questionSynchronization.lock.unlock();

		}
	}

	@Override
	public Integer validateAnswer(int questionNumber, Integer answer) {
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
	public boolean isAnswerCorrect(int questionNumber, Integer answer) {

		answer = validateAnswer(questionNumber, answer);

		if (answer == null) {
			return false;
		} else {
			Question question = getQuestion(questionNumber);
			if (question == null) {
				return false;
			} else {
				return question.getCorrectChoice() == answer;
			}
		}
	}

}
