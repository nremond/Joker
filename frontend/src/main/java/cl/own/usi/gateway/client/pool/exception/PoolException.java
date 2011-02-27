package cl.own.usi.gateway.client.pool.exception;

import cl.own.usi.gateway.client.pool.Pool;

/**
 * Generic exception thrown by the {@link Pool}.
 * 
 * @author bperroud
 *
 */
public class PoolException extends Exception {

	private static final long serialVersionUID = 1L;

	public PoolException(String message) {
		super(message);
	}

	public PoolException(Throwable cause) {
		super(cause);
	}

	public PoolException(String message, Throwable cause) {
		super(message, cause);
	}

}
