package cl.own.usi.cache;

import cl.own.usi.model.User;

/**
 * Simple cache manager interface.
 * 
 * @author bperroud
 * 
 */
public interface CacheManager {

	/**
	 * Get {@link CachedUser} from cache. Return null if user does not exists
	 * 
	 * @param userId
	 * @return
	 */
	CachedUser getCachedUser(String userId);

	/**
	 * Synchronously load a {@link CachedUser}. Internally ensure that
	 * concurrent calls to loadUser for the same userId will only perform one
	 * single call the the backend.
	 * 
	 * Can return null, but this indicate that either the userId does not exists or
	 * a problem occurred during the load.
	 * 
	 * @param userId
	 * @return
	 */
	CachedUser loadUser(String userId);

	/**
	 * Insert a freshly login {@link User} into the cache with all default
	 * values.
	 * 
	 * @param userId
	 */
	void insertFreshlyLoggedUser(String userId);

	/**
	 * Flush cache.
	 */
	void flush();

}
