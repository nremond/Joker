package cl.own.usi.gateway.client.impl.thrift;

import org.apache.thrift.TException;

import cl.own.usi.gateway.client.impl.thrift.WorkerClientThriftImpl.ThriftMultiPool;
import cl.own.usi.gateway.client.impl.thrift.WorkerClientThriftImpl.WorkerHost;
import cl.own.usi.gateway.client.pool.MultiPool;
import cl.own.usi.gateway.client.pool.exception.PoolException;
import cl.own.usi.thrift.WorkerRPC.Client;

/**
 * Wrapper for executing a thrift action.
 *
 * @author bperroud
 * @author nire
 *
 */
abstract class ThriftAction<T> {

	static final int THRIFT_RETRY = 3;

	private MultiPool<WorkerHost, Client> pools = new ThriftMultiPool();

	public ThriftAction(MultiPool<WorkerHost, Client> pools) {
		this.pools = pools;
	}

	abstract protected T action(final Client client) throws TException;

	public T doAction() {

		for (int i = 0; i < THRIFT_RETRY; i++) {
			Client client = getClient();
			try {
				return action(client);
			} catch (TException e) {
				pools.invalidate(client);
				client = null;
			} finally {
				if (client != null) {
					release(client);
				}
			}
		}
		return null;
	}

	private void release(final Client client) {
		try {
			if (client != null) {
				pools.release(client);
			}
		} catch (PoolException e) {

		}
	}

	private Client getClient() {
		try {
			return pools.borrow();
		} catch (PoolException e) {
			throw new IllegalStateException("No pool borrowed...", e);
		}
	}

}