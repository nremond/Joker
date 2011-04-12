package cl.own.usi.cache;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedUser {

	private final String userId;
	private final AtomicBoolean isLogged = new AtomicBoolean(false);
	private final AtomicInteger lastAnsweredQuestion = new AtomicInteger(0);
	private final AtomicInteger score = new AtomicInteger(0);

	public CachedUser(final String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public boolean setLogged() {
		return isLogged.compareAndSet(false, true);
	}

	public boolean setLastAnswerdQuestion(int questionNumber) {
		int tmpLastAnsweredQuestion = lastAnsweredQuestion.get();
		if (tmpLastAnsweredQuestion < questionNumber) {
			return lastAnsweredQuestion.compareAndSet(tmpLastAnsweredQuestion,
					questionNumber);
		} else {
			return false;
		}
	}

	public int getLastAnsweredQuestion() {
		return lastAnsweredQuestion.get();
	}

	public void setScore(int newScore) {
		score.set(newScore);
	}

	public int getScore() {
		return score.get();
	}

	public boolean isLogged() {
		return isLogged.get();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CachedUser other = (CachedUser) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

}
