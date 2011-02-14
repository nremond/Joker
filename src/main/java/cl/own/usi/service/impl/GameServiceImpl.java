package cl.own.usi.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.dao.GameDAO;
import cl.own.usi.model.Game;
import cl.own.usi.model.Question;
import cl.own.usi.service.ExecutorUtil;
import cl.own.usi.service.GameService;

@Service
public class GameServiceImpl implements GameService {

	@Autowired
	ExecutorUtil executorUtil;
	
	@Autowired
	GameDAO gameDAO;
	
	Map<Question, QuestionSynchronization> questionSynchronizations;
	
	volatile int currentQuestionRequest = 0;
	volatile int currentQuestionAnswer = 0;
	
	final ExecutorService executorService = Executors.newFixedThreadPool(2);
 
	public boolean insertGame(int usersLimit, int questionTimeLimit, int pollingTimeLimit, 
			List<Map<String, Map<String, Boolean>>> questions) {
		
		resetPreviousGame();
		
		Game game = gameDAO.insertGame(usersLimit, questionTimeLimit, pollingTimeLimit, mapToQuestion(questions));
		
		questionSynchronizations = new HashMap<Question, QuestionSynchronization>(game.getQuestions().size());
		
		for (Question question : game.getQuestions()) {
			questionSynchronizations.put(question, new QuestionSynchronization(game.getUsersLimit()));
		}
		
		executorService.execute(new StartOfNewQuestion());
		
		return true;
	}
	
	private void resetPreviousGame() {
		// TODO : test if previous game exists and is running.
		
		Game previousGame = gameDAO.getGame();
		if (previousGame != null) {
			
//			for (QuestionSynchronization )
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
		if (questionNumber < 1 || questionNumber > gameDAO.getGame().getQuestions().size()) {
			return null;
		} else {
			return gameDAO.getGame().getQuestions().get(questionNumber - 1);
		}
	}
	
	private QuestionSynchronization getQuestionSync(int questionNumber) {
		Question question = getQuestion(questionNumber);
		if (question != null) {
			return questionSynchronizations.get(question);
		} else {
			return null;
		}
	}

	public boolean waitOtherUsers(int questionNumber) throws InterruptedException {
		QuestionSynchronization questionSync = getQuestionSync(questionNumber);
		if (questionSync == null) {
			return false;
		} else {
			boolean enter = questionSync.questionsReadyLatch.await(gameDAO.getGame().getPollingTimeLimit(), TimeUnit.SECONDS);
			return enter;
		}
	}

	public boolean userEnter(int questionNumber) {
		QuestionSynchronization questionSync = getQuestionSync(questionNumber);
		if (questionSync == null || questionSync.questionRunning || !validateQuestionToRequest(questionNumber)) {
			return false;
		} else {
			questionSync.userEnterLatch.countDown();
			return true;
		}
	}
	
	public boolean userAnswer(int questionNumber) {
		QuestionSynchronization questionSync = getQuestionSync(questionNumber);
		if (questionSync == null || !questionSync.questionRunning || !validateQuestionToAnswer(questionNumber)) {
			return false;
		} else {
			questionSync.userAnswerLatch.countDown();
			return true;
		}
	}
	
	private class StartOfNewQuestion implements Runnable {

		public void run() {
			
			try {
				if (currentQuestionRequest == gameDAO.getGame().getQuestions().size()) {
					return;
				}
				
				currentQuestionRequest++;
				
				QuestionSynchronization questionSynch = getQuestionSync(currentQuestionRequest);
				
				// Wait till the correct number of users join the game
				questionSynch.userEnterLatch.await();
								
				currentQuestionAnswer++;
				questionSynch.questionRunning = true;
				
				executorService.execute(new EndOfNewQuestionWorker());
				
				questionSynch.questionsReadyLatch.countDown();
				
				executorService.execute(new StartOfNewQuestion());
				
			} catch (InterruptedException e) {
				
			}
			
		}
		
	}
	
	private class EndOfNewQuestionWorker implements Runnable {

		public void run() {
			
			Game game = gameDAO.getGame();
			int questionTimeLimit = game.getQuestionTimeLimit();
				
			try {
				QuestionSynchronization questionSynch = getQuestionSync(currentQuestionAnswer);
				
				questionSynch.userAnswerLatch.await(questionTimeLimit, TimeUnit.SECONDS);
				
				questionSynch.questionRunning = false;
				
				if (currentQuestionAnswer == game.getQuestions().size()) {
					// TODO : twittService.twitt("Notre Appli supporte " + game.getUsersLimit() + " joueurs #challengeUSI2011");
				}
			} catch (InterruptedException e) {
				
				// TODO : terminate game.
				
			}
		}
		
	}

	public int getQuestionNumberToRequest() {
		return currentQuestionRequest;
	}
	
	public int getQuestionNumberToAnswer() {
		return currentQuestionAnswer;
	}

	protected Game getGame() {
		return gameDAO.getGame();
	}

	private boolean validateQuestionToRequest(int questionNumber) {
		return questionNumber == currentQuestionRequest;
	}

	private boolean validateQuestionToAnswer(int questionNumber) {
		return questionNumber == currentQuestionAnswer;
	}
	
	private class QuestionSynchronization {
		
		private volatile boolean questionRunning = false;
		
		private final CountDownLatch userEnterLatch;
		private final CountDownLatch questionsReadyLatch;
		private final CountDownLatch userAnswerLatch;
		
		public QuestionSynchronization(int usersLimit) {
			userEnterLatch = new CountDownLatch(usersLimit);
			questionsReadyLatch = new CountDownLatch(1);
			userAnswerLatch = new CountDownLatch(usersLimit);
		}
		
	}

}
