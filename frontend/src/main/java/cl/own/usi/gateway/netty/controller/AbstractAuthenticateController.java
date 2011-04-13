package cl.own.usi.gateway.netty.controller;

import java.util.HashMap;
import java.util.Map;

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
		return StringUtils.equals(this.authenticationKey, authenticationKey);
	}

	protected final Map<String, String> parseQueryString(
			final String queryString) {

		final String[] params = queryString.split("&");
		final Map<String, String> result = new HashMap<String, String>(
				params.length);

		for (String param : params) {
			final String[] keyValue = param.split("=");
			assert keyValue.length == 2;

			final String key = keyValue[0];
			final String value = keyValue[1];

			result.put(key, value);
		}

		return result;
	}
}
