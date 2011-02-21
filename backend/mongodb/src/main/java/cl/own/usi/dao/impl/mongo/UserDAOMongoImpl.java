package cl.own.usi.dao.impl.mongo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import cl.own.usi.dao.UserDAO;
import cl.own.usi.model.Answer;
import cl.own.usi.model.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

@Repository
public class UserDAOMongoImpl implements UserDAO {

	@Autowired
	DB db;

	private static String usersCollection = "users";
	private static DBObject emailIndex = new BasicDBObject("email", 1);

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public boolean insertUser(User user) {
		DBCollection dbUsers = db.getCollection(usersCollection);

		// the driver keeps a cache of the added index
		dbUsers.ensureIndex(emailIndex, "emailIndex", true);

		BasicDBObject dbUser = new BasicDBObject();
		dbUser.put("email", "tristan@fuckyoudear.com");
		dbUser.put("password", "topsecret!");
		dbUser.put("firstname", "Tristan");
		dbUser.put("lastname", "Doze");
		dbUser.put("score", "3");

		WriteResult wr = dbUsers.insert(dbUser);
		String error = wr.getError();

		// E11000 -> duplicate key
		if (StringUtils.hasText(error) && error.indexOf("E11000") == 0) {
			logger.debug("user "+user.getEmail()+" was already in the collection, insertion aborted");
			return false;
		} else {
			logger.debug("user "+user.getEmail()+" was successfully inserted");
			return true;
		}
	}

	@Override
	public User getUserById(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertRequest(User user, int questionNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertAnswer(User user, Answer answer) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Answer> getAnswers(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String login(String email, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logout(User user) {
		// TODO Auto-generated method stub

	}

	@Override
	public void flushUsers() {
		// TODO Auto-generated method stub

	}

}
