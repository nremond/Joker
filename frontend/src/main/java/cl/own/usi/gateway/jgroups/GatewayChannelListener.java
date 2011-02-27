package cl.own.usi.gateway.jgroups;

import java.io.Serializable;

import org.jgroups.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.gateway.client.WorkerClient;
import cl.own.usi.jgroups.DefaultNotificationBusAwareConsumer;
import cl.own.usi.worker.WorkerState;

/**
 * Gateway channel listener for JGroups notifications. Handle the addition and
 * removal of worker nodes based on received notifications.
 *
 * @author reynald
 */
@Component
public class GatewayChannelListener extends DefaultNotificationBusAwareConsumer {

	@Autowired
	private WorkerClient workerClient;

	@Override
	public void memberJoined(final Address mbr) {
		if (!getNotificationBus().getLocalAddress().equals(mbr)) {
			logger.info("*** MEMBER JOINED: {}, requesting state transfer", mbr);

			final Serializable memberState = getNotificationBus()
					.getCacheFromMember(mbr, 10000, 2);

			logger.info("State for member {} is {}", mbr, memberState);

			if (memberState instanceof WorkerState) {
				final WorkerState workerState = (WorkerState) memberState;
				if (workerState.isServing()) {
					workerClient.addWorkerNode(workerState.getLocalAddress()
							.getHostAddress(), workerState.getListeningPort());
				} else {
					logger.warn("TODO: host is not serving requests yet, should"
							+ " reschedule state retrieval in a few seconds!");
				}

			} else {
				logger.warn("Received an unknown node state {} from {}",
						memberState, mbr);
			}

		} else {
			logger.info("*** RECEIVED JOIN MESSAGE FOR OWN NODE ***");
		}
	}

	@Override
	public void memberLeft(final Address mbr) {
		logger.info("*** MEMBER LEFT: {}", mbr);
	}

}
