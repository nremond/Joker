package cl.own.usi.main;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class WorkerMain {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WorkerMain.class);

	public static void main(String[] args) {

		final String[] springContextFile = new String[] {
				"classpath*:workerApplication.xml",
				"classpath*:jgroupsConfig.xml" };
		LOGGER.info("Starting up Worker...");

		final ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				springContextFile);

		final Date startupDate = new Date(applicationContext.getStartupDate());
		LOGGER.info("Started at {}", startupDate);
	}
}
