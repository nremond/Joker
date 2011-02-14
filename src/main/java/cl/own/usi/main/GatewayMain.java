package cl.own.usi.main;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class GatewayMain {

	public static void main(String[] args) {

		String springContextFile = "classpath*:application.xml";
		new ClassPathXmlApplicationContext(springContextFile);

	}
}
