package cl.own.usi.gateway.client.pool.exception;

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
