package cl.own.usi.jgroups;

import java.io.Serializable;

import org.jgroups.Address;
import org.jgroups.blocks.NotificationBus;
import org.jgroups.blocks.NotificationBus.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNotificationBusAwareConsumer implements Consumer {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private NotificationBus notificationBus;

	public final void setNotificationBus(final NotificationBus notificationBus) {
		this.notificationBus = notificationBus;
	}

	protected final NotificationBus getNotificationBus() {
		return notificationBus;
	}

	/**
	 * Default implementation: log in debug level the received notification.<br />
	 * <br />
	 *
	 * @see {@link Consumer#handleNotification(Serializable)}
	 */
	@Override
	public void handleNotification(final Serializable n) {
		logger.debug("** Received notification: " + n);
	}

	/**
	 * Default implementation: return null.<br />
	 * <br />
	 *
	 * @see {@link Consumer#getCache()}
	 */
	@Override
	public Serializable getCache() {
		return null;
	}

	/**
	 * Default implementation: do nothing.<br />
	 * <br />
	 *
	 * @see {@link Consumer#memberJoined(Address)}
	 */
	@Override
	public void memberJoined(final Address mbr) {
		// nothing
	}

	/**
	 * Default implementation: do nothing.<br />
	 * <br />
	 *
	 * @see {@link Consumer#memberLeft(Address)}
	 */
	@Override
	public void memberLeft(Address mbr) {
		// nothing
	}
}
