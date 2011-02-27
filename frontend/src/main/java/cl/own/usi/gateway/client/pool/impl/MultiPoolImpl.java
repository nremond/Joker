package cl.own.usi.gateway.client.pool.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cl.own.usi.gateway.client.pool.MultiPool;
import cl.own.usi.gateway.client.pool.ObjectPoolFactory;
import cl.own.usi.gateway.client.pool.Pool;
import cl.own.usi.gateway.client.pool.exception.FactoryException;
import cl.own.usi.gateway.client.pool.exception.PoolException;
import cl.own.usi.gateway.client.pool.policy.ObjectValidationPolicy;
import cl.own.usi.gateway.client.pool.policy.PoolSelectionPolicy;

/**
 * {@link MultiPool} implementation using java.util.concurrent primitives.
 * 
 * @author bperroud
 *
 * @param <K>
 * @param <V>
 */
public abstract class MultiPoolImpl<K, V> implements MultiPool<K, V> {

	private ObjectPoolFactory<V> factory;
	private ObjectValidationPolicy objectValidationPolicy = ObjectValidationPolicy.VALIDATE_NONE;

	protected final List<K> keys = new ArrayList<K>();
	private final ConcurrentMap<K, Pool<V>> pools = new ConcurrentHashMap<K, Pool<V>>();
	
	private final ConcurrentMap<V, K> borrowedClients = new ConcurrentHashMap<V, K>();

	private final ConcurrentMap<K, AtomicInteger> errors = new ConcurrentHashMap<K, AtomicInteger>();
	
	private final AtomicBoolean active = new AtomicBoolean(true);
	
	public V borrow() throws PoolException {
		
		if (!active.get()) {
			throw new PoolException("Pool is not active");
		}
		
		V object = null;
		int retry = 0;
		
		do {
			K key = null;
			try {
				key = getKey();
				if (key == null) {
					throw new PoolException("No keys defined. Please addKey first");
				}
				Pool<V> pool = pools.get(key);
				object = pool.borrow();
				if (object != null) {
					borrowedClients.put(object, key);
					errors.get(key).set(0);
				}
			} catch (FactoryException e) {
				if (key != null) {
					removeKey(key);
				}
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
			int errorsCount = errors.get(key).incrementAndGet();
			if (errorsCount == pool.getMaxPoolSize()) {
				removeKey(key);
			}
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
			errors.put(key, new AtomicInteger(0));
		}
	}

	public synchronized void removeKey(K key) {
		if (keys.remove(key)) {
			
			errors.remove(key);
			Pool<V> pool = pools.remove(key);
			pool.shutdown();
			
		}
	}
	
	public void setPoolSelectionPolicy(PoolSelectionPolicy poolSelectionPolicy) {
		// TODO Auto-generated method stub
		
	}

	abstract protected K getKey();
	
	abstract protected Pool<V> createPool(K key);
	
	public void shutdown() {
		if (active.compareAndSet(true, false)) {
			for (Map.Entry<K, Pool<V>> entry : pools.entrySet()) {
				entry.getValue().shutdown();
			}
		}
	}
	
	public int getMaxPoolSize() {
		return 0;
	}
}
