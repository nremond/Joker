package cl.own.usi.main;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class WorkerMain {

	public static void main(String[] args) {

		String springContextFile = "classpath*:workerApplication.xml";
		new ClassPathXmlApplicationContext(springContextFile);

	}

}
