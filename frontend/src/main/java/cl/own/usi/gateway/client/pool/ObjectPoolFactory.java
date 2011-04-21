package cl.own.usi.gateway.client.pool;

import cl.own.usi.gateway.client.pool.exception.FactoryException;
import cl.own.usi.gateway.client.pool.exception.PoolException;

/**
 * Pool factory. Responsible for the lifecycle of pooled objects.
 * 
 * @author bperroud
 *
 * @param <V>
 */
public interface ObjectPoolFactory<V> {

	V create() throws FactoryException;
	
	boolean validate(V object) throws PoolException;
	
	void destroy(V object);
	
}
