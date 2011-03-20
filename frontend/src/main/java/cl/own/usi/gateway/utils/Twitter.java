package cl.own.usi.gateway.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Twitter wrapper that use twitter4j as twitter client.
 * 
 * Instanciate everything needed, tweet and release stuff (hopefully).
 * 
 * @author bperroud
 * 
 */
public class Twitter {

	private static final String OAuthConsumerKey = "5l0h8kHhpOKS6nAQQujPA";
	private static final String OAuthConsumerSecret = "f79UKb2bYe43HJr3pePRzmDvYtZC8fpPFQOKegVRdI";
	private static final String OAuthAccessToken = "266003317-e3HcR7MaK9F0rhJWnvGNiqlCXL9xljoRKdArqmVA";
	private static final String OAuthAccessTokenSecret = "bgC9ydvBbEs7dbntAPfQgn9RxckVRkyp7EQMfQ";

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public void twitt(String status) {

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(OAuthConsumerKey)
				.setOAuthConsumerSecret(OAuthConsumerSecret)
				.setOAuthAccessToken(OAuthAccessToken)
				.setOAuthAccessTokenSecret(OAuthAccessTokenSecret);

		twitter4j.Twitter twitter = new TwitterFactory(cb.build())
				.getInstance();

		try {
			twitter.updateStatus(status);
		} catch (TwitterException e) {
			LOGGER.warn("Twitter exception while trying to twit " + status, e);
		} finally {
			if (twitter != null) {
				twitter.shutdown();
			}
		}

	}

}
