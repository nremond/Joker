package cl.own.usi.network;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Small utility class around InetAddress.
 *
 * @author reynald
 */
public class InetAddressHelper {

	private InetAddressHelper() {
		// utility class => hide default constructor
	}

	private static InetAddress localAddress;

	/**
	 * Locate and return the current public {@link InetAddress}. In case of
	 * multi-homed machine, will return the first found public interface without
	 * any sorting done.
	 *
	 * @return current public {@link InetAddress}
	 */
	public static InetAddress getCurrentIP() {
		if (localAddress == null) {
			localAddress = locateAddress();
		}

		return localAddress;
	}

	private static InetAddress locateAddress() {
		try {
			final Enumeration<NetworkInterface> nets = NetworkInterface
					.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				final List<InterfaceAddress> interfaceAddresses = netint
						.getInterfaceAddresses();
				for (InterfaceAddress interfaceAddress : interfaceAddresses) {
					if (!interfaceAddress.getAddress().isLoopbackAddress()) {
						return interfaceAddress.getAddress();
					}
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		throw new RuntimeException("Unable to locate network interfaces ?!?");
	}
}
