package cl.own.usi.gateway.netty.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractAuthenticateController extends AbstractController {

	static final String AUTHENTIFICATION_KEY_NAME = "authentication_key";

	private String authenticationKey;

	@Value(value = "${frontend.authenticationkey:1234}")
	public void setAuthentificationKey(final String authentificationKey) {
		this.authenticationKey = authentificationKey;
	}

	protected final boolean isAuthenticationKeyValid(
			final String authenticationKey) {
		return StringUtils
				.equals(this.authenticationKey, authenticationKey);
	}
}
