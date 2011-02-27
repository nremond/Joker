package cl.own.usi.worker;

import java.io.Serializable;
import java.net.InetAddress;

import com.google.common.base.Objects;

/**
 * Worker state class. Represent the current state of a worker node.
 *
 * @author reynald
 */
public class WorkerState implements Serializable {

	/**
	 * Do not forget to increment at each change of this class!
	 */
	private static final long serialVersionUID = 1L;

	private final InetAddress localAddress;

	private final int listeningPort;

	private final boolean serving;

	public WorkerState(final InetAddress localAddress, final int listeningPort,
			final boolean serving) {
		super();
		this.localAddress = localAddress;
		this.listeningPort = listeningPort;
		this.serving = serving;
	}

	public final InetAddress getLocalAddress() {
		return localAddress;
	}

	public final int getListeningPort() {
		return listeningPort;
	}

	public final boolean isServing() {
		return serving;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("localAddress", localAddress)
				.add("listeningPort", listeningPort).add("serving", serving)
				.toString();
	}
}
