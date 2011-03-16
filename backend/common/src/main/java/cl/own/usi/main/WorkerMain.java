package cl.own.usi.main;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class WorkerMain {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WorkerMain.class);

	private WorkerMain() {
		// no need for a constructor
	}

	public static void main(String[] args) {

		final String[] springContextFile = new String[] {
				"classpath*:spring/workerApplication.xml",
				"classpath*:spring/dao.xml", };
		LOGGER.info("Starting up Worker...");

		final ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				springContextFile);

		final Date startupDate = new Date(applicationContext.getStartupDate());
		LOGGER.info("Started at {}", startupDate);
	}
}
