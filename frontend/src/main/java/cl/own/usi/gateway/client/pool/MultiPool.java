package cl.own.usi.gateway.client.pool;

import cl.own.usi.gateway.client.pool.policy.PoolSelectionPolicy;

/**
 * Pool of pool interface. key => pool<V>
 * 
 * @author bperroud
 *
 * @param <K>
 * @param <V>
 */
public interface MultiPool<K, V> extends Pool<V> {
	
	boolean addKey(K key);
	
	void setPoolSelectionPolicy(PoolSelectionPolicy poolSelectionPolicy);
	
}
