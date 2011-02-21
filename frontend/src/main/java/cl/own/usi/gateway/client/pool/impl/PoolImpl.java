package cl.own.usi.gateway.client.pool.impl;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cl.own.usi.gateway.client.pool.ObjectPoolFactory;
import cl.own.usi.gateway.client.pool.Pool;
import cl.own.usi.gateway.client.pool.exception.FactoryException;
import cl.own.usi.gateway.client.pool.exception.PoolException;
import cl.own.usi.gateway.client.pool.policy.ObjectValidationPolicy;


public class PoolImpl<V> implements Pool<V> {
	
	private final static int DEFAULT_MAX_POOL_SIZE = 8;
	private final static long DEFAULT_MAX_WAIT_ON_OBJECT_MILLI= 500;
		
	private final BlockingQueue<V> freeObjectsQueue;
	private final AtomicInteger currentPoolSize = new AtomicInteger(0);
	private final Set<V> borrowedObjects = Collections.newSetFromMap(new ConcurrentHashMap<V, Boolean>());
	
	private final int maxPoolSize;
	private final long maxWaitOnObjectMilli;
	
	private ObjectValidationPolicy objectValidationPolicy = ObjectValidationPolicy.VALIDATE_NONE;
	
	private ObjectPoolFactory<V> factory = null;
	
	public PoolImpl() {
		this(DEFAULT_MAX_POOL_SIZE);
	}
	public PoolImpl(int maxPoolSize) {
		this(maxPoolSize, DEFAULT_MAX_WAIT_ON_OBJECT_MILLI);
	}
	public PoolImpl(int maxPoolSize, long maxWaitOnObjectMilli) {
		this.maxPoolSize = maxPoolSize;
		this.maxWaitOnObjectMilli = maxWaitOnObjectMilli;
		freeObjectsQueue = new ArrayBlockingQueue<V>(maxPoolSize);
	}
	
	public V borrow() throws PoolException {
		return borrow0(true);
	}
	
	private V borrow0(boolean recursif) throws PoolException {
		
		V object = freeObjectsQueue.poll();
		
		int retry = 0;
		while (object == null && retry < MAX_AQUISITION_RETRY) {
			if (currentPoolSize.get() < maxPoolSize) {
				object = create();
			} else {
				try {
					object = freeObjectsQueue.poll(maxWaitOnObjectMilli, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					throw new IllegalStateException();
				}
			}
		}
		
		if (objectValidationPolicy.validateOnBorrow() && !factory.validate(object)) {
			invalidate0(object);
			object = recursif ? borrow0(false) : null;
		} else {
			borrowedObjects.add(object);
		}
		
		return object;
		
	}
	
	private V create() throws FactoryException {
		int newPoolSize = currentPoolSize.incrementAndGet();
		if (newPoolSize <= maxPoolSize) {
			return factory.create();
		} else {
			currentPoolSize.decrementAndGet();
			return null;
		}
	}

	public void release(V object) throws PoolException {
		if (borrowedObjects.remove(object)) {
			
			if (objectValidationPolicy.validateOnRelease()
					&& !factory.validate(object)) {
				invalidate0(object);
			} else {
				if (!freeObjectsQueue.offer(object)) {
					invalidate0(object);
				}
			}
		}
	}

	public void invalidate(V object) {
		if (borrowedObjects.remove(object)) {
			invalidate0(object);
		}
	}
	
	private void invalidate0(V object) {
		factory.destroy(object);
		currentPoolSize.decrementAndGet();
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

}
