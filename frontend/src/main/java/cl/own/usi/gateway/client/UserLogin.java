package cl.own.usi.gateway.client;

public class UserLogin {
	private final String userId;
	private final boolean isAlreadyLogged;

	public UserLogin(final String userId, final boolean isAlreadyLogged) {
		this.userId = userId;
		this.isAlreadyLogged = isAlreadyLogged;
	}

	public String getUserId() {
		return userId;
	}

	public boolean isAlreadyLogged() {
		return isAlreadyLogged;
	}

}