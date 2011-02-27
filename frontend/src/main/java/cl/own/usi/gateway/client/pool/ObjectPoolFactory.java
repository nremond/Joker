package cl.own.usi.gateway.client.pool;

import cl.own.usi.gateway.client.pool.exception.FactoryException;

/**
 * Pool factory. Responsible for the lifecycle of pooled objects.
 * 
 * @author bperroud
 *
 * @param <V>
 */
public interface ObjectPoolFactory<V> {

	V create() throws FactoryException;
	
	boolean validate(V object) throws FactoryException;
	
	void destroy(V object);
	
}
