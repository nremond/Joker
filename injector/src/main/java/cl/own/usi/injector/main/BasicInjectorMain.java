package cl.own.usi.injector.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

/**
 * Multithreaded game injector.
 * 
 * Increase NBUSERS and NBQUESTIONS to add load.
 * 
 * Game creation, user insertion, question answers are based on
 * {@link HttpClient} (OIO), and question request is based on
 * {@link AsyncHttpClient} (NIO). {@link UserGameWorker#questionRecieved()} is
 * called asynchronously when the server has sent the question.
 * 
 * One {@link UserGameWorker} is instanciated for each user, and store the state
 * of this user.
 * 
 * @author bperroud
 * 
 */
public class BasicInjectorMain {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BasicInjectorMain.class);

	/*
	 * Server parameters
	 */
	private final static String DEFAULT_HOST = "localhost";
	private final static int DEFAULT_PORT = 9080;

	private static String HOST = DEFAULT_HOST;
	private static int PORT = DEFAULT_PORT;

	/*
	 * Game parameters
	 */
	private final static boolean FLUSHUSERSTABLE = true;
	private final static int DEFAULT_NBUSERS = 10;
	private final static int NBQUESTIONS = 5;
	private final static int QUESTIONTIMEFRAME = 60;
	private final static int SYNCHROTIME = 12;
	private final static int LOGINTIMEOUT = 600;

	private static int NBUSERS = DEFAULT_NBUSERS;
	private static int MAXNOFILES = 395240;

	private static long SLA = 350L;

	/*
	 * Synchronization and concurrency stuff
	 */
	// Executor that will run players' sequences. Thread pool of number of
	// processors * 2. No need to be bigger.
	private final static ExecutorService executor = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
	// List of players' workers
	private static List<UserGameWorker> workers;

	private final static CountDownLatch gameStartSynchroLatch = new CountDownLatch(
			1);

	private static CountDownLatch gamersHaveAnsweredAllQuestions;

	private static CountDownLatch gameFinishedSynchroLatch;

	// Shared async http client, because it run internal workers and lot of
	// heavy stuff.
	private static final AsyncHttpClientConfig.Builder ASYNC_HTTP_CLIENT_CONFIG_BUILDER = new AsyncHttpClientConfig.Builder();
	static {
		ASYNC_HTTP_CLIENT_CONFIG_BUILDER
				.setMaximumConnectionsPerHost(MAXNOFILES);
		ASYNC_HTTP_CLIENT_CONFIG_BUILDER.setMaximumConnectionsTotal(MAXNOFILES);
		ASYNC_HTTP_CLIENT_CONFIG_BUILDER.setRequestTimeoutInMs(Math.max(QUESTIONTIMEFRAME + SYNCHROTIME, LOGINTIMEOUT) * 1000 * 2);
		ASYNC_HTTP_CLIENT_CONFIG_BUILDER.setConnectionTimeoutInMs(Math.min(QUESTIONTIMEFRAME + SYNCHROTIME, LOGINTIMEOUT) * 1000);
	}
	
	private static final AsyncHttpClient asyncHttpClient = new AsyncHttpClient(
			ASYNC_HTTP_CLIENT_CONFIG_BUILDER.build());

	/**
	 * @param args
	 * @throws IOException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException,
			InterruptedException, ExecutionException {

		if (args.length > 0) {
			HOST = args[0];
		}
		if (args.length > 1) {
			PORT = Integer.valueOf(args[1]);
		}
		if (args.length > 2) {
			NBUSERS = Integer.valueOf(args[2]);
		}

		workers = new ArrayList<BasicInjectorMain.UserGameWorker>(NBUSERS);
		
		// 1 and not NBUSERS in case we loose players in the way.
		gamersHaveAnsweredAllQuestions = new CountDownLatch(1); 
		gameFinishedSynchroLatch = new CountDownLatch(1);

		createGame();

		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			return;
		}
		
		insertUsers(NBUSERS);

		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			return;
		}

		for (UserGameWorker worker : workers) {
			executor.execute(worker);
		}

		LOGGER.info("Let's start");

		// Let all workers start login
		gameStartSynchroLatch.countDown();

		gamersHaveAnsweredAllQuestions.await();

		LOGGER.info("All gamers have answered all questions, let's wait synchrotime");

		Thread.sleep((QUESTIONTIMEFRAME + SYNCHROTIME) * 1000);

		LOGGER.info("Reinsert all workers in the queue to request ranking");

		long starttime = System.currentTimeMillis();

		for (UserGameWorker worker : workers) {
			executor.execute(worker);
		}

		// Wait till all workers has finished the game.
		gameFinishedSynchroLatch.await();

		// shutdown everything cleanly.
		executor.shutdown();
		executor.awaitTermination(600, TimeUnit.SECONDS);
		asyncHttpClient.close();

		long stoptime = System.currentTimeMillis();

		LOGGER.info("Ranking requests done in {} ms, shutting down",
				(stoptime - starttime));

	}

	/**
	 * Insert players. Read players from 1million_users_1.csv file.
	 * 
	 * @param limit
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private static void insertUsers(Integer limit) throws IOException,
			InterruptedException, ExecutionException {

		long starttime = System.currentTimeMillis();

		File file = new File("../tools/1million_users_1.csv");
		HttpClient httpClient = new HttpClient();

		BufferedReader reader = new BufferedReader(new FileReader(file));

		try {
			String line;
			boolean first = true;
			int lineNumber = 1;
			while ((line = reader.readLine()) != null) {
				// skip first line
				if (first) {
					first = false;
					continue;
				}

				lineNumber++;

				if (limit != null && limit < lineNumber - 1) {
					break;
				}

				String[] fields = line.split(",");

				if (fields.length != 4) {
					LOGGER.error("Error with line {} : {}", lineNumber, line);
					continue;
				}

				String postUrl = "http://" + HOST + ":" + PORT + "/api/user";

				String postBody = "{ \"firstname\" : \"" + fields[0]
						+ "\", \"lastname\" : \"" + fields[1]
						+ "\", \"mail\" : \"" + fields[2]
						+ "\", \"password\" : \"" + fields[3] + "\" }";

				PostMethod post = new PostMethod(postUrl);
				post.setRequestBody(postBody);

				try {
					int httpResponseCode = httpClient.executeMethod(post);
					if (httpResponseCode == 201) {
						workers.add(new UserGameWorker(QUESTIONTIMEFRAME,
								SYNCHROTIME, NBQUESTIONS, fields[2], fields[3],
								executor));
					}

				} finally {
					post.releaseConnection();
				}

			}
		} finally {
			reader.close();
		}

		long stoptime = System.currentTimeMillis();

		LOGGER.info("Users inserted in {} ms", (stoptime - starttime));
	}

	/**
	 * Create game. All game parameters are given as constants.
	 * 
	 * @throws HttpException
	 * @throws IOException
	 */
	private static void createGame() throws HttpException, IOException {

		long starttime = System.currentTimeMillis();

		HttpClient httpClient = new HttpClient();

		String postBody = "{ \"authentication_key\" : \"1234\", \"parameters\" : { \"questions\" "
				+ ": [ { \"goodchoice\" : 1, \"label\" : \"Question1\", \"choices\" : [ \""
				+ "choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question2\""
				+ ", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\""
				+ " : \"Question3\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\""
				+ " : 4, \"label\" : \"Question4\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] },"
				+ " { \"goodchoice\" : 1, \"label\" : \"Question5\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\","
				+ " \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question6\", \"choices\" : [ \"choix1\", \"choix2\","
				+ " \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 3, \"label\" : \"Question7\", \"choices\" : [ \"choix1\","
				+ " \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question8\", \"choices\" : ["
				+ " \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 4, \"label\" : \"Question9\", \"ch"
				+ "oices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Question1"
				+ "0\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" :"
				+ " \"Question11\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\""
				+ " : 3, \"label\" : \"Question12\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, "
				+ "{ \"goodchoice\" : 4, \"label\" : \"Question13\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", "
				+ "\"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question14\", \"choices\" : [ \"choix1\", \"choix"
				+ "2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question15\", \"choices\" : [ \"ch"
				+ "oix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 1, \"label\" : \"Question16\", \"choi"
				+ "ces\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"label\" : \"Questi"
				+ "on17\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoice\" : 2, \"lab"
				+ "el\" : \"Question18\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, { \"goodchoi"
				+ "ce\" : 1, \"label\" : \"Question19\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"choix4\" ] }, "
				+ "{ \"goodchoice\" : 3, \"label\" : \"Question20\", \"choices\" : [ \"choix1\", \"choix2\", \"choix3\", \"ch"
				+ "oix4\" ] } ], \"parameters\" : { \"logintimeout\" : "
				+ LOGINTIMEOUT
				+ ", \"synchrotime\" : "
				+ SYNCHROTIME
				+ ", \"nbusersthreshold\" : "
				+ NBUSERS
				+ ", "
				+ "\"questiontimeframe\" : "
				+ QUESTIONTIMEFRAME
				+ ", \"nbquestions\" : "
				+ NBQUESTIONS
				+ ", "
				+ "\"flushusertable\" : "
				+ FLUSHUSERSTABLE
				+ ", \"trackeduseridmail\" : \"unused\" } } }";

		String postUrl = "http://" + HOST + ":" + PORT + "/api/game";

		PostMethod post = new PostMethod(postUrl);
		post.setRequestBody(postBody);

		try {
			int httpResponseCode = httpClient.executeMethod(post);
			LOGGER.debug("Response code : {}", httpResponseCode);

		} finally {
			post.releaseConnection();
		}

		long stoptime = System.currentTimeMillis();

		LOGGER.info("Game created in {} ms", (stoptime - starttime));
	}

	/**
	 * Players class that handle.
	 * 
	 * This class is run by an {@link Executor}. The runnable function do one
	 * step at a time, rescheduling itself after each step. This permit to
	 * emulate concurrency with much lower threads.
	 * 
	 * There is one step to login, one to request the question, one to answer it
	 * (these two steps are repeated as much as needed by the game), and one
	 * last step for the ranking.
	 * 
	 * @author bperroud
	 * 
	 */
	private static class UserGameWorker implements Runnable {

		/*
		 * Game parameters
		 */
		private final int questiontimeframe;
		private final int synchrotime;
		private final int numquestions;
		private final String email;
		private final String password;

		/*
		 * Internal state
		 */
		private String sessionId;
		private Header cookieHeader;
		private int currentQuestion = 1;

		private final ExecutorService executor;

		private final Random random = new Random();
		private volatile boolean currentQuestionRequested = false;

		public UserGameWorker(int questiontimeframe, int synchrotime,
				int numquestions, String email, String password,
				ExecutorService executor) {
			this.questiontimeframe = questiontimeframe;
			this.synchrotime = synchrotime;
			this.numquestions = numquestions;
			this.email = email;
			this.password = password;
			this.executor = executor;
		}

		public void run() {

			try {
				HttpClient httpClient = new HttpClient();

				// If user is not logged, go to the login process.
				if (sessionId == null) {

					try {
						gameStartSynchroLatch.await();
					} catch (InterruptedException e) {
					}

					long starttime = System.currentTimeMillis();

					// login
					String postUrl = "http://" + HOST + ":" + PORT
							+ "/api/login";
					String postBody = "{ \"mail\" : \"" + email
							+ "\", \"password\" : \"" + password + "\" }";

					PostMethod post = new PostMethod(postUrl);
					post.setRequestBody(postBody);

					boolean loginOk = false;

					try {
						int httpResponseCode = httpClient.executeMethod(post);

						if (httpResponseCode == 201) {

							// Retrieve the authentification cookie
							Header header = post
									.getResponseHeader("Set-Cookie");
							if (header != null) {
								String headerValue = header.getValue();
								sessionId = headerValue.substring(
										headerValue.indexOf('=') + 2,
										headerValue.length() - 1);
								cookieHeader = new Header("Cookie", headerValue);
							}

							loginOk = true;

						} else {
							LOGGER.warn(
									"Problem at login {} with response code {}",
									email, httpResponseCode);
						}

					} finally {
						post.releaseConnection();
					}

					long delta = System.currentTimeMillis() - starttime;
					if (delta > SLA) {
						LOGGER.warn("[SLA] Login for user {} took {} ms",
								email, delta);
					}

					if (loginOk) {
						executor.execute(this);
					}

				} else if (currentQuestion <= numquestions) {

					// There is still questions to answer or request

					if (!currentQuestionRequested) {

						// The question has not been requested, need to request
						// it

						String getUrl = "http://" + HOST + ":" + PORT
								+ "/api/question/" + currentQuestion;

						asyncHttpClient
								.prepareGet(getUrl)
								.setHeader("Cookie", cookieHeader.getValue())
								.execute(
										new MyAsyncHandler(this,
												currentQuestion, sessionId));

					} else {

						long starttime = System.currentTimeMillis();

						// The question has been requested, need to answer.

						String postUrl = "http://" + HOST + ":" + PORT
								+ "/api/answer/" + currentQuestion;
						String postBody = "{ \"answer\" : "
								+ (random.nextInt(4) + 1) + " }";

						PostMethod post = new PostMethod(postUrl);
						post.setRequestBody(postBody);
						post.setRequestHeader(cookieHeader);

						try {
							int httpResponseCode = httpClient
									.executeMethod(post);

							if (httpResponseCode == 201) {

								// OK :)

							} else {
								LOGGER.error(
										"Error answering the question with response code {}",
										httpResponseCode);
							}

						} finally {
							post.releaseConnection();
						}

						long delta = System.currentTimeMillis() - starttime;
						if (delta > SLA) {
							LOGGER.warn(
									"[SLA] Answer question {} for user {} took {} ms",
									new Object[] { currentQuestion, email,
											delta });
						}

						currentQuestion++;
						currentQuestionRequested = false;
						if (currentQuestion <= numquestions) {
							executor.execute(this);
						} else {
							gamersHaveAnsweredAllQuestions.countDown();
						}
					}

				} else {

					long starttime = System.currentTimeMillis();
					// The game is finished, just need to request ranking.
					String getUrl = "http://" + HOST + ":" + PORT
							+ "/api/ranking";

					GetMethod get = new GetMethod(getUrl);
					get.setRequestHeader(cookieHeader);

					try {
						int httpResponseCode = httpClient.executeMethod(get);

						if (httpResponseCode == 200) {

							String body = get.getResponseBodyAsString();

							LOGGER.info(
									"Everything went fine for user {} : {}",
									email, body);

						} else {
							LOGGER.error(
									"Error requesting the ranking with response code {}",
									httpResponseCode);
						}

					} finally {
						get.releaseConnection();
					}

					long delta = System.currentTimeMillis() - starttime;
					if (delta > SLA) {
						LOGGER.warn(
								"[SLA] Ranking request for user {} took {} ms",
								email, delta);
					}

					gameFinishedSynchroLatch.countDown();

				}

			} catch (IOException e) {
				LOGGER.error("IOException", e);
			}
		}

		/**
		 * Callback function when the question is received.
		 * 
		 */
		public void questionReceived(int questionRequested) {

			LOGGER.debug("Question {} received", questionRequested);

			currentQuestionRequested = true;

			executor.execute(this);

		}

	}

	/**
	 * {@link AsyncHttpClient} handler that will call
	 * {@link UserGameWorker#questionRecieved()} when the request returns.
	 * 
	 * @author bperroud
	 * 
	 */
	private static class MyAsyncHandler extends AsyncCompletionHandler<Integer> {

		private final UserGameWorker worker;
		private final int questionRequested;
		private final String userId;

		public MyAsyncHandler(UserGameWorker worker, int questionRequested,
				String userId) {
			this.worker = worker;
			this.questionRequested = questionRequested;
			this.userId = userId;
		}

		@Override
		public Integer onCompleted(Response response) throws Exception {

			if (response != null && response.getStatusCode() == 200) {
				worker.questionReceived(questionRequested);
				return 200;
			} else {
				LOGGER.warn(
						"Question {} request completed, but recieve wrong response code {} for user {}",
						new Object[] { questionRequested,
								response.getStatusCode(), userId });
				return -1;
			}
		}

		@Override
		public void onThrowable(Throwable t) {
			super.onThrowable(t);
			LOGGER.error("Throwable on getting question", t);
		}

	}

}
