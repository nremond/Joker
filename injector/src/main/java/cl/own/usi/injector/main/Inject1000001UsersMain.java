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
	private final static int CONCURRENT_WORKERS = 4; // Runtime.getRuntime().availableProcessors() * 2
	private final static int NUMBER_OF_USERS_TO_INSERT = 100; //Integer.MAX_VALUE;
	
	private static final ExecutorService executor = Executors
			.newFixedThreadPool(CONCURRENT_WORKERS);
	private static final BlockingQueue<String> fileLines = new LinkedBlockingQueue<String>(1000);

	public static void main(String[] args) throws IllegalArgumentException,
			IOException, InterruptedException, ExecutionException {

		LOGGER.info("Starting up Injector ... ");

		File userFile = new File("../tools/1million_users_1.csv");
		BufferedReader reader = new BufferedReader(new FileReader(userFile));
		
		for (int i = 0; i < CONCURRENT_WORKERS; i++) {
			executor.execute(new InsertionWorker());
		}
		
		long starttime = System.currentTimeMillis();
		
		try {
			String line;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				if (i >= NUMBER_OF_USERS_TO_INSERT) {
					break;
				}
				fileLines.offer(line, 10, TimeUnit.SECONDS);
				i++;
			}
		} finally {
			reader.close();
		}

		executor.shutdown();
		executor.awaitTermination(3600, TimeUnit.SECONDS);
		
		long stoptime = System.currentTimeMillis();
		
		LOGGER.info(" ... done in {} ms", (stoptime - starttime));
	}

	private static class InsertionWorker implements Runnable {

		@Override
		public void run() {

			HttpClient httpClient = new HttpClient();
			
			try {
				String line;
				while ((line = fileLines.poll(10, TimeUnit.SECONDS)) != null) {
					String[] fields = line.split(",");
					if (fields.length == 4) {
	
						String postUrl = "http://" + HOST + ":" + PORT
								+ "/api/user";
	
						String postBody = "{ \"firstname\" : \"" + fields[0]
								+ "\", \"lastname\" : \"" + fields[1]
								+ "\", \"mail\" : \"" + fields[2]
								+ "\", \"password\" : \"" + fields[3] + "\" }";
	
						PostMethod post = new PostMethod(postUrl);
						post.setRequestBody(postBody);
						try {
							int httpResponseCode = httpClient.executeMethod(post);
							if (httpResponseCode != 201) {
								LOGGER.error("Error inserting {}", line);
							}
						} finally {
							post.releaseConnection();
						}
					}
				}
			} catch (InterruptedException e) {
				
			} catch (IOException e){
				
			}
		}
	}
}
