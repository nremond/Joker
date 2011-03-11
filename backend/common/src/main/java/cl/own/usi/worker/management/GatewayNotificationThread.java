package cl.own.usi.worker.management;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GatewayNotificationThread implements Runnable, InitializingBean {

	private int initialDelay = 2;
	private int period = 30;
	private String gatewayHost = "localhost";
	private int gatewayPort = 9080;
	
	private String workerHost;
	private int workerPort = 7911;
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	NetworkReachable workerMain;
	
	@Override
	public void run() {
		
		HttpClient client = new HttpClient();
		client.setHostConfiguration(hostConfig);
		
		HttpMethod method = new GetMethod("/api/join/" + workerHost + "/" + workerPort);
		
		try {
			LOGGER.debug("Ping gateway to be reachable at {}:{}", workerHost, workerPort);
			int response = client.executeMethod(method);
			if (response != HttpResponseStatus.CREATED.getCode()) {
				LOGGER.warn("Gateway response an unexpected code {}", response);
			}
		} catch (IOException e) {
			LOGGER.warn("Error while trying to ping the gateway at {}:{}", gatewayHost, gatewayPort);
			LOGGER.warn("IOException", e);
		} finally {
			method.releaseConnection();
		}
		
	}

	private ScheduledExecutorService executor;
	private HostConfiguration hostConfig = new HostConfiguration();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		hostConfig.setHost(gatewayHost, gatewayPort);
		
		workerHost = workerMain.getHost();
		workerPort = workerMain.getPort();
		
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(this, initialDelay, period, TimeUnit.SECONDS);
	}

}
