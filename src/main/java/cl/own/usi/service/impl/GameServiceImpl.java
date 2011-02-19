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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.dao.GameDAO;
import cl.own.usi.gateway.netty.RequestHandler;
import cl.own.usi.model.Game;
import cl.own.usi.model.Question;
import cl.own.usi.service.ExecutorUtil;
import cl.own.usi.service.GameService;

@Service
public class GameServiceImpl implements GameService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	ExecutorUtil executorUtil;
	
	@Autowired
	GameDAO gameDAO;
	
	final ExecutorService executorService = Executors.newFixedThreadPool(2);
	
	GameSynchronization gameSynchronization;
 
	public boolean insertGame(int usersLimit, int questionTimeLimit, int pollingTimeLimit, 
			List<Map<String, Map<String, Boolean>>> questions) {
		
		resetPreviousGame();
		
		Game game = gameDAO.insertGame(usersLimit, questionTimeLimit, pollingTimeLimit, mapToQuestion(questions));
		
		gameSynchronization = new GameSynchronization(game);
		
		executorService.execute(new StartOfNewQuestionWorker(gameSynchronization));
		
		return true;
	}
	
	private void resetPreviousGame() {
		GameSynchronization oldGameSynchronization = gameSynchronization;
		if (oldGameSynchronization != null) {
			oldGameSynchronization.currentQuestionToAnswer = 0;
			for (Map.Entry<Question, QuestionSynchronization> entry : oldGameSynchronization.questionSynchronizations.entrySet()) {
				QuestionSynchronization questionSynchronization = entry.getValue();
				questionSynchronization.questionReadyLatch.countDown();
				for (int i = 0; i < oldGameSynchronization.game.getUsersLimit(); i++) {
					questionSynchronization.allUsersAnswerLatch.countDown();
				}
				// TODO : remove thread from pool.
			}
		}
	}

	private List<Question> mapToQuestion(List<Map<String, Map<String, Boolean>>> questions) {
		List<Question> list = new ArrayList<Question>();
		int number = 1;
		for (Map<String, Map<String, Boolean>> element : questions) {
			for (Map.Entry<String, Map<String, Boolean>> entry : element.entrySet()) {
				Question question = new Question();
				question.setNumber(number);
				question.setLabel(entry.getKey());
				question.setChoices(new ArrayList<String>(entry.getValue().size()));
				int i = 1;
				for (Map.Entry<String, Boolean> answer : entry.getValue().entrySet()) {
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

	public boolean waitOtherUsers(int questionNumber) throws InterruptedException {
		QuestionSynchronization questionSync = getQuestionSync(questionNumber);
		if (questionSync == null) {
			return false;
		} else {
			boolean enter = questionSync.questionReadyLatch.await(gameDAO.getGame().getPollingTimeLimit(), TimeUnit.SECONDS);
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
			
			logger.debug("Start game");
			try {
				logger.debug("Wait on all users");
				gameSynchronization.enoughUsersLatch.await();
				logger.debug("Enough users join the game");
			} catch (InterruptedException e) {
				logger.warn("Interrupted", e);
			}
			
			for (int i = 1; i <= gameSynchronization.game.getQuestions().size(); i++) {
			
				logger.info("Starting question " + i + ". Response question number " + gameSynchronization.currentQuestionToAnswer);
				QuestionSynchronization questionSynchronization = gameSynchronization.getQuestionSynchronization(i);
				
				questionSynchronization.questionRunning = true;
				
				for (Runnable r : questionSynchronization.waitingQueue) {
					logger.debug("Inserting a early requester to the working queue");
					executorUtil.getExecutorService().execute(r);
				}
				
				// Wait till the correct number of users join the game
				questionSynchronization.questionReadyLatch.countDown();
				
				try {
					logger.debug("Wait to all users answer, or till the timeout" + gameSynchronization.game.getQuestionTimeLimit());
					
					// Wait either all users answer the question, or the time limit
					boolean reachedZero = questionSynchronization.allUsersAnswerLatch.await(gameSynchronization.game.getQuestionTimeLimit(), TimeUnit.SECONDS);
					questionSynchronization.questionRunning = false;
					if (reachedZero) {
						logger.debug("All users has answered, going to the next question.");
					} else {
						logger.debug("Normal completion of the game, going further.");
					}
					
				} catch (InterruptedException e) {
					logger.warn("Interrupted", e);
				}
				
				logger.info("Question " + i + " finished, going to the next question");
				
				gameSynchronization.currentQuestionToAnswer++;
				
			}
			
			logger.info("All questions finished, tweet and clean everything");
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
			return questionNumber > 0 && questionNumber <= gameSynchronization.game.getQuestions().size();
		}
	}
	
	
	private class GameSynchronization {
		
		private final CountDownLatch enoughUsersLatch;

		private final Map<Question, QuestionSynchronization> questionSynchronizations;
		
		volatile int currentQuestionToAnswer = 1;
		
		private final Game game;
		
		public GameSynchronization(Game game) {
			
			this.game = game;
			
			questionSynchronizations = new HashMap<Question, QuestionSynchronization>(game.getQuestions().size());
			
			for (Question question : game.getQuestions()) {
				questionSynchronizations.put(question, new QuestionSynchronization(game.getUsersLimit()));
			}
			
			enoughUsersLatch = new CountDownLatch(game.getUsersLimit());
		}
		
//		QuestionSynchronization getCurrentQuestionSynchronization() {
//			return questionSynchronizations.get(game.getQuestions().get(currentQuestionToAnswer - 1));
//		}
		
		QuestionSynchronization getQuestionSynchronization(int questionNumber) {
			return questionSynchronizations.get(game.getQuestions().get(questionNumber - 1));
		}
	}
	
	private class QuestionSynchronization {
		
		private volatile boolean questionRunning = false;
		
		private final CountDownLatch questionReadyLatch;
		private final CountDownLatch allUsersAnswerLatch;
		
		private final Queue<Runnable> waitingQueue = new LinkedBlockingQueue<Runnable>();
		
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
	
	
	public void scheduleQuestionReply(RequestHandler.QuestionWorker questionWorker) {
		
		if (questionWorker.getQuestionNumber() <= gameSynchronization.currentQuestionToAnswer) {
			executorUtil.getExecutorService().execute(questionWorker);
		} else {
			QuestionSynchronization questionSynchronization = getQuestionSync(questionWorker.getQuestionNumber());
			
			// TODO : lock here.
			logger.info("Too early question request for quesition " + questionWorker.getQuestionNumber() + ", putting in a temporaray queue");
			questionSynchronization.waitingQueue.offer(questionWorker);
			
			if (questionWorker.getQuestionNumber() <= gameSynchronization.currentQuestionToAnswer) {
				logger.error("Race condition for question " + questionWorker.getQuestionNumber());
			}
		}
	}

}
