package cl.own.usi.jgroups;

import org.jgroups.blocks.NotificationBus;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Notification bus bean.Spring wiring around the jgroups
 * {@link NotificationBus} building block class.
 *
 * @author reynald
 */
public class NotificationBusBean implements DisposableBean, InitializingBean {

	private String configurationFile;
	private String busName;
	private NotificationBus notificationBus;

	@Autowired(required = false)
	private DefaultNotificationBusAwareConsumer consumer;

	@Override
	public void destroy() throws Exception {
		if (notificationBus != null) {
			notificationBus.stop();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(configurationFile, "'configurationFile' is undefined!");
		Assert.hasText(busName, "'busName' is undefined!");

		notificationBus = new NotificationBus(busName, configurationFile);

		if (consumer != null) {
			notificationBus.setConsumer(consumer);
			consumer.setNotificationBus(notificationBus);
		}

		notificationBus.start();
	}

	public void setConfigurationFile(final String configurationFile) {
		this.configurationFile = configurationFile;
	}

	public void setBusName(final String busName) {
		this.busName = busName;
	}
}
