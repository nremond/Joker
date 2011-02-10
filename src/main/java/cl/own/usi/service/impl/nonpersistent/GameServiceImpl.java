package cl.own.usi.service.impl.nonpersistent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.own.usi.model.Game;
import cl.own.usi.model.Question;
import cl.own.usi.service.ExecutorUtil;
import cl.own.usi.service.GameService;

@Service
public class GameServiceImpl implements GameService {

	@Autowired
	ExecutorUtil executorUtil;
	
	Game game;
	
	int currentQuestion = 0;
	
	long currentQuestionStartTime = 0L;
	
	volatile boolean currentQuestionRunning = false;
	
	volatile CountDownLatch latch;
	
	private final ScheduledExecutorService scheduler = 
	       Executors.newScheduledThreadPool(1);
 
	public boolean insertGame(int usersLimit, int questionTimeLimit, int pollingTimeLimit, 
			List<Map<String, Map<String, Boolean>>> questions) {
		
		game = new Game();
		
		game.setUsersLimit(usersLimit);
		game.setQuestionTimeLimit(questionTimeLimit);
		game.setPollingTimeLimit(pollingTimeLimit);
		
		game.setQuestions(mapToQuestion(questions));
		
		endOfCurrentQuestion();
		
		return true;
	}
	
	private List<Question> mapToQuestion(List<Map<String, Map<String, Boolean>>> questions) {
		List<Question> list = new ArrayList<Question>();
		int number = 0;
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

	public int getCorrectAnswerForQuestion(int questionNumber) {
		return game.getQuestions().get(questionNumber).getCorrectChoice();
	}

	public void startOfCurrentQuestion() {
		
		currentQuestionStartTime = System.nanoTime();
		currentQuestionRunning = true;
		
	}
	
	private void initLatch() {
		latch = new CountDownLatch(game.getUsersLimit());
	}
	
	public void endOfCurrentQuestion() {
		
		System.out.println("endOfCurrentQuestion");
		
		currentQuestionRunning = false;
		
		if (game.getQuestions().size() > currentQuestion) {
			currentQuestion++;
		}
		
		initLatch();
		
		executorUtil.getExecutorService().execute(timerWorker);
		
	}

	public Question getCurrentQuestion() {
		return game.getQuestions().get(currentQuestion);
	}

	public int getCorrectAnswerForCurrentQuestion() {
		return getCurrentQuestion().getCorrectChoice();
	}

	public boolean waitOtherUsers() throws InterruptedException {
		
		if (latch.await(game.getPollingTimeLimit(), TimeUnit.SECONDS)) {
			return true;
		} else {
			return false;
		}
	}

	public void userEnter() {
		getLatch().countDown();
	}

	protected CountDownLatch getLatch() {
		return latch;
	}

	QuestionTimerTask questionTimerTask = new QuestionTimerTask();
	TimerWorker timerWorker = new TimerWorker();
	
	private class TimerWorker implements Runnable {
		
		public void run() {
			try {

				getLatch().await();
				
				startOfCurrentQuestion();
				
				scheduler.schedule(questionTimerTask, game.getQuestionTimeLimit(), TimeUnit.SECONDS);
				
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	private class QuestionTimerTask extends TimerTask {
		
		@Override
		public void run() {
			endOfCurrentQuestion();
		}
	}

	public int getCurrentQuestionNumber() {
		return currentQuestion;
	}

	public Game getGame() {
		return game;
	}

	public long getStartOfCurrentQuestion() {
		return currentQuestionStartTime;
	}
}
