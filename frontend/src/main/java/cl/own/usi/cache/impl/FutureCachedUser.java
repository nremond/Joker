package cl.own.usi.cache.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import cl.own.usi.cache.CachedUser;

public class FutureCachedUser implements Future<CachedUser> {

	private final AtomicBoolean done = new AtomicBoolean(false);
	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	private final CountDownLatch latch = new CountDownLatch(1);
	private final AtomicReference<CachedUser> cachedUserReference = new AtomicReference<CachedUser>();
	
	@Override
	public boolean cancel(boolean arg0) {
		if (!isDone() && cancelled.compareAndSet(false, true)) {
			latch.countDown();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public CachedUser get() throws InterruptedException, ExecutionException {
		if (isCancelled()) {
			return null;
		} else if (!isDone()) {
			latch.await();
		}
		return cachedUserReference.get();
	}

	@Override
	public CachedUser get(long arg0, TimeUnit arg1)
			throws InterruptedException, ExecutionException,
			TimeoutException {
		if (isCancelled()) {
			return null;
		} else if (!isDone()) {
			latch.await(arg0, arg1);
		}
		return cachedUserReference.get();
	}

	@Override
	public boolean isCancelled() {
		return cancelled.get();
	}

	@Override
	public boolean isDone() {
		return done.get();
	}
	
	public void setCachedUser(CachedUser cachedUser) {
		if (!isDone() && !isCancelled() && cachedUserReference.compareAndSet(null, cachedUser)) {
			done.set(true);
			latch.countDown();
		}
	}
}