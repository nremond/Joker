package cl.own.usi.gateway.client.pool.exception;

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
