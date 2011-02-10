package cl.own.usi.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class ExecutorUtil implements InitializingBean{

	private ExecutorService executorService;

	int poolSize = 2;
	
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
	
	public int getPoolSize() {
		return poolSize;
	}
	
	public void afterPropertiesSet() throws Exception {
		
		executorService = Executors.newFixedThreadPool(getPoolSize());
		
	}
	
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
}
