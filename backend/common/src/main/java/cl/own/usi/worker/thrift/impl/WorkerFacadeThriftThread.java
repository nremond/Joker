package cl.own.usi.worker.thrift.impl;

import org.apache.thrift.server.TServer;

public class WorkerFacadeThriftThread extends Thread {

	private final TServer server;
	private boolean serving = false;

	public WorkerFacadeThriftThread(final TServer server) {
		super("Thrift serving thread");

		assert server != null;

		this.server = server;
	}

	public final boolean isServing() {
		return serving;
	}

	@Override
	public void run() {
		serving = true;
		try {
			server.serve();
		} catch (Exception e) {
			serving = false;
		}
	}
	
	public void requestShutdown() {
		server.stop();
		serving = false;
	}

}
