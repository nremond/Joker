package cl.own.usi.gateway.client.pool.exception;

import cl.own.usi.gateway.client.pool.ObjectPoolFactory;

/**
 * Exception thrown by the {@link ObjectPoolFactory}
 * 
 * @author bperroud
 *
 */
public class FactoryException extends PoolException {
	
	private static final long serialVersionUID = 1L;

	public FactoryException(String message) {
		super(message);
	}

	public FactoryException(Throwable cause) {
		super(cause);
	}

	public FactoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
