package cl.own.usi.dao.impl.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * Class that allow execution of a
 * {@link DBCollection#findAndModify(DBObject, DBObject, DBObject, boolean, DBObject, boolean, boolean)}
 * action while correctly handling stale config errors reported by Mongo. If
 * this error is reported, the action is tried multiple times before failing.
 *
 * @author reynald
 */
public class FindAndModifyAction {

	/**
	 * com.mongodb.CommandResult$CommandFailure: command failed [command failed
	 * [findandmodify] { "errmsg" :
	 * "exception: ns: joker.users findOne has stale config" , "code" : 9996 ,
	 * "ok" : 0.0}
	 */
	private static final int STALE_CONFIG_ERROR_CODE = 9996;

	private static final int RETRY = 3;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FindAndModifyAction.class);

	private final DBCollection collection;
	private final DBObject query;
	private final DBObject fields;
	private final DBObject sort;
	private final boolean remove;
	private final DBObject update;
	private final boolean returnNew;
	private final boolean upsert;

	public FindAndModifyAction(final DBCollection collection,
			final DBObject query, final DBObject fields, final DBObject sort,
			final boolean remove, final DBObject update,
			final boolean returnNew, final boolean upsert) {

		super();
		this.collection = collection;
		this.query = query;
		this.fields = fields;
		this.sort = sort;
		this.remove = remove;
		this.update = update;
		this.returnNew = returnNew;
		this.upsert = upsert;
	}

	public final DBObject safeAction() {
		MongoException lastException = null;

		for (int i = 0; i < RETRY; i++) {

			try {
				final DBObject result = collection.findAndModify(query, fields,
						sort, remove, update, returnNew, upsert);
				return result;

			} catch (MongoException me) {
				lastException = me;

				// Let's try multiple times if we have a stale config
				if (me.getCode() != STALE_CONFIG_ERROR_CODE) {
					throw me;
				} else {
					LOGGER.debug(
							"MongoException caught while doing findAndModify",
							me);
				}
			}
		}

		throw lastException;
	}
}
