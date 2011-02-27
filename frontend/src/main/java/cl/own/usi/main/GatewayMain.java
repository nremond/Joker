package cl.own.usi.main;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class for the gateway.
 * 
 * Run it !!
 * 
 * @author bperroud
 *
 */
public class GatewayMain {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GatewayMain.class);

	public static void main(String[] args) {

		final String springContextFile = "classpath*:gatewayApplication.xml";

		LOGGER.info("Starting up Gateway...");

		final ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				springContextFile);

		final Date startupDate = new Date(applicationContext.getStartupDate());
		LOGGER.info("Started at {}", startupDate);
	}
}
