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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GatewayNotificationThread implements Runnable, InitializingBean {

	@Value(value = "${notificationThread.initialDelay:2}")
	private int initialDelay = 2;

	@Value(value = "${notificationThread.period:30}")
	private int period = 30;

	@Value(value = "${notificationThread.gatewayHost:localhost}")
	private String gatewayHost = "localhost";

	@Value(value = "${notificationThread.gatewayPort:9080}")
	private int gatewayPort = 9080;

	private String workerHost;
	private int workerPort = 7911;

	@Autowired
	private NetworkReachable workerMain;

	private ScheduledExecutorService executor;
	private HostConfiguration hostConfig = new HostConfiguration();

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GatewayNotificationThread.class);

	@Override
	public void run() {

		final HttpClient client = new HttpClient();
		client.setHostConfiguration(hostConfig);

		final HttpMethod method = new GetMethod("/api/join/" + workerHost + "/"
				+ workerPort);

		try {
			LOGGER.debug("Ping gateway to be reachable at {}:{}", workerHost,
					workerPort);
			int response = client.executeMethod(method);
			if (response != HttpResponseStatus.CREATED.getCode()) {
				LOGGER.warn("Gateway response an unexpected code {}", response);
			}
		} catch (IOException e) {
			LOGGER.warn("Error while trying to ping the gateway at {}:{}",
					gatewayHost, gatewayPort);
			LOGGER.warn("IOException", e);
		} finally {
			method.releaseConnection();
		}

	}

	@Override
	public void afterPropertiesSet() throws Exception {

		hostConfig.setHost(gatewayHost, gatewayPort);

		workerHost = workerMain.getHost();
		workerPort = workerMain.getPort();

		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(this, initialDelay, period,
				TimeUnit.SECONDS);

		LOGGER.info(
				"Ping gateway at {}:{} to announce worker node every {} second(s)",
				new Object[] { gatewayHost, gatewayPort, period });
	}
}
