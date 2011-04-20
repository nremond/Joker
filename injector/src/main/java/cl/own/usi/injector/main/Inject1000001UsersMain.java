package cl.own.usi.injector.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple class that insert all users given in tools/1million_users_1.csv file.
 *
 * Insert lines in a blocking queue, and multithreaded workers read the queue.
 *
 * @author bperroud
 *
 */
public class Inject1000001UsersMain {

	private static final long SLA = 350L;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Inject1000001UsersMain.class);

	/*
	 * Server parameters
	 */
	private final static String HOST = "localhost";
	private final static int PORT = 9080;

	/*
	 * Concurrency parameters
	 */
	private final static int CONCURRENT_WORKERS = Runtime.getRuntime()
			.availableProcessors() * 8;
	private final static int NUMBER_OF_USERS_TO_INSERT = Integer.MAX_VALUE;

	private static final ExecutorService executor = Executors
			.newFixedThreadPool(CONCURRENT_WORKERS);
	private static final BlockingQueue<String> fileLines = new LinkedBlockingQueue<String>(
			1000);

	public static void main(String[] args) throws IllegalArgumentException,
			IOException, InterruptedException, ExecutionException {

		String host = HOST;
		int port = PORT;
		int nbusers = NUMBER_OF_USERS_TO_INSERT;

		if (args.length > 0) {
			host = args[0];
		}
		if (args.length > 1) {
			port = Integer.valueOf(args[1]);
		}
		if (args.length > 2) {
			nbusers = Integer.valueOf(args[2]);
		}

		LOGGER.info("Starting up Injector for at max {} users ... ", nbusers);

		String filepath = System.getProperty("file", "../tools/1million_users_1.csv");
		
		File userFile = new File(filepath);
		BufferedReader reader = new BufferedReader(new FileReader(userFile));

		for (int i = 0; i < CONCURRENT_WORKERS; i++) {
			executor.execute(new InsertionWorker(host, port));
		}

		long starttime = System.currentTimeMillis();

		try {
			String line;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				if (i >= nbusers) {
					break;
				}
				fileLines.offer(line, 100, TimeUnit.SECONDS);
				i++;
			}
		} finally {
			reader.close();
		}

		executor.shutdown();
		executor.awaitTermination(7200, TimeUnit.SECONDS);

		long stoptime = System.currentTimeMillis();

		LOGGER.info(" ... done in {} ms", (stoptime - starttime));
	}

	private static class InsertionWorker implements Runnable {

		private final String host;
		private final int port;

		public InsertionWorker(final String host, final int port) {
			this.host = host;
			this.port = port;
		}

		@Override
		public void run() {

			HttpClient httpClient = new HttpClient();

			try {
				String line;
				while ((line = fileLines.poll(10, TimeUnit.SECONDS)) != null) {
					String[] fields = line.split(",");
					if (fields.length == 4) {

						String postUrl = "http://" + host + ":" + port
								+ "/api/user";

						String postBody = "{ \"firstname\" : \"" + fields[0]
								+ "\", \"lastname\" : \"" + fields[1]
								+ "\", \"mail\" : \"" + fields[2]
								+ "\", \"password\" : \"" + fields[3] + "\" }";

						PostMethod post = new PostMethod(postUrl);
						post.setRequestBody(postBody);

						final long starttime = System.currentTimeMillis();

						try {
							int httpResponseCode = httpClient
									.executeMethod(post);
							if (httpResponseCode != 201) {
								LOGGER.error(
										"Error inserting {}, httpResponseCode is {} ",
										line, httpResponseCode);
							}
						} finally {
							post.releaseConnection();
						}

						long delta = System.currentTimeMillis() - starttime;
						if (delta > SLA) {
							LOGGER.warn(
									"[SLA] User creation for user {} took {} ms",
									fields[2], delta);
						}
					}
				}
			} catch (InterruptedException e) {

			} catch (IOException e) {

			}
		}
	}
}
