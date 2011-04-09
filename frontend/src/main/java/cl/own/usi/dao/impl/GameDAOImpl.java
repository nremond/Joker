package cl.own.usi.dao.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import cl.own.usi.dao.GameDAO;
import cl.own.usi.model.Game;
import cl.own.usi.model.Question;

/**
 * In memory implementation of {@link GameDAO}.
 * 
 * @author bperroud
 * 
 */
@Repository
public class GameDAOImpl implements GameDAO, InitializingBean {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GameDAOImpl.class);

	private static final String STOREFILE = "joker.game";

	private Game game;

	private String storePath;

	@Value(value = "${frontend.storePath:/tmp}")
	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}

	public Game insertGame(int usersLimit, int questionTimeLimit,
			int pollingTimeLimit, int synchroTimeLimit, int numberOfQuestion,
			List<Question> questions) {

		game = new Game();

		game.setUsersLimit(usersLimit);
		game.setQuestionTimeLimit(questionTimeLimit);
		game.setPollingTimeLimit(pollingTimeLimit);
		game.setSynchroTimeLimit(synchroTimeLimit);
		game.setNumberOfQuestion(numberOfQuestion);

		game.setQuestions(new ArrayList<Question>(questions));

		persistCurrentGame();
		
		return game;
	}

	public Question getQuestion(int questionNumber) {
		if (game == null || questionNumber < 1
				|| questionNumber > getGame().getQuestions().size()) {
			return null;
		} else {
			return getGame().getQuestions().get(questionNumber - 1);
		}
	}

	public Game getGame() {
		return game;
	}

	@Override
	public void refreshCache() {
		// nothing to do as it's not persisted here
	}

	private void persistCurrentGame() {
		if (game != null) {
			
			File storedGame = getGameFileStore();

			if (storedGame.canWrite()) {
				
				byte[] serializedGame = serialize(game);
				if (serializedGame != null) {
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(storedGame);
						fos.write(serializedGame);
	
					} catch (IOException e) {
						LOGGER.error("Error while trying to persist the game", e);
					} finally {
						if (fos != null) {
							try {
								fos.close();
							} catch (IOException e) { LOGGER.warn("IOException at closing", e); } 
						}
					}
				}
			}
		}
	}
	
	private void loadCurrentGame() {
		
		File storedGame = getGameFileStore();
		
		if (storedGame.canRead()) {
			
			FileInputStream fis = null;
			
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				fis = new FileInputStream(storedGame);
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = fis.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				game = (Game)deserialize(baos.toByteArray());
				LOGGER.info("Game {} loaded !", game);
				
				baos.close();
			} catch (IOException e) {
				LOGGER.error("Error while trying to load the game", e);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) { LOGGER.warn("IOException at closing", e); }
				}
			}
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {

		loadCurrentGame();
		
	}
	
	private File getGameFileStore() {
		return new File(storePath + '/' + STOREFILE);
	}

	private static byte[] serialize(Object obj) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(out);
			oout.writeObject(obj);
			oout.close();
			return out.toByteArray();
		} catch (IOException e) {
			LOGGER.warn("Serialization error", e);
		}
		return null;
	}

	private static Object deserialize(final byte[] value) {
		try {
			final ByteArrayInputStream in = new ByteArrayInputStream(value);
			final ObjectInputStream oin = new ObjectInputStream(in);
			final Object retval = oin.readObject();
			oin.close();
			return retval;
		} catch (Exception e) {
			LOGGER.warn("Deserialization error", e);
		}
		return null;
	}

}
