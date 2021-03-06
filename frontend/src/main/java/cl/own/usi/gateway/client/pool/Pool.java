package cl.own.usi.gateway.client.pool;

import cl.own.usi.gateway.client.pool.exception.PoolException;
import cl.own.usi.gateway.client.pool.policy.ObjectValidationPolicy;

/**
 * Pool interface. The basic contract for using the pool is :
 * 
 * try {
 * 		o = borrow()
 * 		... use o ...
 * } catch (Exception) {
 * 		invalidate(o)
 * } finally {
 * 		release(o)
 * }
 * 
 * @author bperroud
 *
 * @param <V>
 */
public interface Pool<V> {

	final static int MAX_AQUISITION_RETRY = 3;

	V borrow() throws PoolException;
	
	void release(V object) throws PoolException;
	
	void invalidate(V object);
	
	void setFactory(ObjectPoolFactory<V> factory);
	
	void setObjectValidationPolicy(ObjectValidationPolicy objectValidationPolicy);
	
	int getMaxPoolSize();
	
	void shutdown();
	
}
