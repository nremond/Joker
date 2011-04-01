package cl.own.usi.cache.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.cache.CacheManager;
import cl.own.usi.cache.CachedUser;
import cl.own.usi.gateway.client.ExtendedUserInfoAndScore;
import cl.own.usi.gateway.client.WorkerClient;

@Component
public class CacheManagerImpl implements CacheManager {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CacheManagerImpl.class);

	private final ConcurrentMap<String, CachedUser> cachedUsers = new ConcurrentHashMap<String, CachedUser>();

	private final ConcurrentMap<String, FutureCachedUser> processingUsers = new ConcurrentHashMap<String, FutureCachedUser>();

	@Autowired
	private WorkerClient workerClient;

	public CachedUser getCachedUser(final String userId) {
		return cachedUsers.get(userId);
	}

	public void insertFreshlyLoggedUser(final String userId) {
		final CachedUser cachedUser = new CachedUser(userId);
		cachedUser.setLogged();
		cachedUsers.putIfAbsent(userId, cachedUser);
	}

	@Override
	public void flush() {
		cachedUsers.clear();
	}

	@Override
	public CachedUser loadUser(final String userId) {

		final CachedUser cachedUser = cachedUsers.get(userId);

		if (cachedUser == null) {
			FutureCachedUser futureCachedUser = getOrCreateFuture(userId);

			try {
				return futureCachedUser.get();
			} catch (InterruptedException e) {
				return null;
			} catch (ExecutionException e) {
				LOGGER.warn("ExceutionException happens", e);
				return null;
			}

		} else {
			return cachedUser;
		}
	}

	/**
	 * Create {@link Future<CachedUser>} ensuring that only one exists for the
	 * same key.
	 * 
	 * @param userId
	 * @return
	 */
	private FutureCachedUser getOrCreateFuture(final String userId) {

		FutureCachedUser futureCachedUser = processingUsers.get(userId);

		if (futureCachedUser == null) {
			futureCachedUser = new FutureCachedUser();
			final FutureCachedUser tmpCachedUserFuture = processingUsers
					.putIfAbsent(userId, futureCachedUser);
			if (tmpCachedUserFuture == null) {
				internalLoadUser(userId, futureCachedUser);
				return futureCachedUser;
			} else {
				return tmpCachedUserFuture;
			}
		} else {
			return futureCachedUser;
		}
	}

	private void internalLoadUser(final String userId,
			final FutureCachedUser futureCachedUser) {

		// double check that CachedUser is not been inserted into cachedUsers.
		CachedUser cachedUser = cachedUsers.get(userId);

		if (cachedUser != null) {
			futureCachedUser.setCachedUser(cachedUser);
		} else {

			final ExtendedUserInfoAndScore extendedUserInfoAndScore = workerClient
					.getExtendedUserInfo(userId);

			if (extendedUserInfoAndScore != null
					&& extendedUserInfoAndScore.getUserId() != null) {

				cachedUser = map(extendedUserInfoAndScore);

				futureCachedUser.setCachedUser(cachedUser);

			} else {
				futureCachedUser.cancel(false);
			}
		}
	}

	private static CachedUser map(
			final ExtendedUserInfoAndScore extendedUserInfoAndScore) {
		final CachedUser cachedUser = new CachedUser(
				extendedUserInfoAndScore.getUserId());
		cachedUser.setLastAnswerdQuestion(extendedUserInfoAndScore
				.getLatestQuestionNumberAnswered());
		cachedUser.setScore(extendedUserInfoAndScore.getScore());
		if (extendedUserInfoAndScore.isLogged()) {
			cachedUser.setLogged();
		}
		return cachedUser;
	}

}
