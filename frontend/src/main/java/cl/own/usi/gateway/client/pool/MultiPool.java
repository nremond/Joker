package cl.own.usi.gateway.client.pool;

import cl.own.usi.gateway.client.pool.policy.PoolSelectionPolicy;

public interface MultiPool<K, V> extends Pool<V> {
	
	void addKey(K key);
	
	void setPoolSelectionPolicy(PoolSelectionPolicy poolSelectionPolicy);
	
}
