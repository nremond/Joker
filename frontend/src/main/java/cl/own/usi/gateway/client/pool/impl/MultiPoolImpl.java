package cl.own.usi.gateway.client.pool.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cl.own.usi.gateway.client.pool.MultiPool;
import cl.own.usi.gateway.client.pool.ObjectPoolFactory;
import cl.own.usi.gateway.client.pool.Pool;
import cl.own.usi.gateway.client.pool.exception.PoolException;
import cl.own.usi.gateway.client.pool.policy.ObjectValidationPolicy;
import cl.own.usi.gateway.client.pool.policy.PoolSelectionPolicy;


public abstract class MultiPoolImpl<K, V> implements MultiPool<K, V> {

	private ObjectPoolFactory<V> factory;
	private ObjectValidationPolicy objectValidationPolicy = ObjectValidationPolicy.VALIDATE_NONE;

	protected List<K> keys = new ArrayList<K>();
	ConcurrentMap<K, Pool<V>> pools = new ConcurrentHashMap<K, Pool<V>>();
	
	ConcurrentMap<V, K> borrowedClients = new ConcurrentHashMap<V, K>();
	
	public V borrow() throws PoolException {
		
		V object = null;
		int retry = 0;
		
		do {
			K key = getKey();
			Pool<V> pool = pools.get(key);
			object = pool.borrow();
			if (object != null) {
				borrowedClients.put(object, key);
			}
		} while (object == null && retry++ < MAX_AQUISITION_RETRY);
		
		return object;
	}

	public void release(V object) throws PoolException {
		K key = borrowedClients.remove(object);
		if (key != null) {
			Pool<V> pool = pools.get(key);
			pool.release(object);
		}
	}

	public void invalidate(V object) {
		K key = borrowedClients.remove(object);
		if (key != null) {
			Pool<V> pool = pools.get(key);
			pool.invalidate(object);
		}
	}

	public void setFactory(ObjectPoolFactory<V> factory) {
		if (this.factory != null) {
			throw new IllegalArgumentException();
		} else {
			this.factory = factory;
		}
	}

	public void setObjectValidationPolicy(
			ObjectValidationPolicy objectValidationPolicy) {
		if (this.objectValidationPolicy != null) {
			throw new IllegalArgumentException();
		} else {
			this.objectValidationPolicy = objectValidationPolicy;
		}
	}

	public synchronized void addKey(K key) {
		if (!keys.contains(key)) {
			keys.add(key);
			pools.put(key, createPool(key));
		}
	}

	public void setPoolSelectionPolicy(PoolSelectionPolicy poolSelectionPolicy) {
		// TODO Auto-generated method stub
		
	}

	abstract protected K getKey();
	
	abstract protected Pool<V> createPool(K key);
	
//	Random r = new Random();
//	protected K getKey() {
//		return keys.get(r.nextInt(keys.size()));
//	}
//	
//	protected Pool<V> createPool(K key) {
//		Pool<V> pool = new PoolImpl<V>();
//		pool.setFactory(factory);
//		return pool;
//	}
}
