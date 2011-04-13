package cl.own.usi.tests.controller;

import java.util.Map;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

import cl.own.usi.gateway.netty.controller.AuditController;

public class AuditControllerTest extends AuditController {

	@Test
	public void testPattern() {
		String entry = "/toto";
		Assert.assertFalse(entry, URI_PATTERN.matcher(entry).matches());

		entry = "/api/audit";
		Assert.assertFalse(entry, URI_PATTERN.matcher(entry).matches());

		entry = "/api/audit?toto_123=titi";
		Matcher match = URI_PATTERN.matcher(entry);
		Assert.assertTrue(entry, match.matches());
		Assert.assertEquals(2, match.groupCount());
		Assert.assertNull(match.group(1));
		Assert.assertEquals("toto_123=titi", match.group(2));

		entry = "/api/audit/2?auth_key=123&param=value";
		match = URI_PATTERN.matcher(entry);
		Assert.assertTrue(entry, match.matches());
		Assert.assertEquals(2, match.groupCount());
		Assert.assertEquals("2", match.group(1));
		Assert.assertEquals("auth_key=123&param=value", match.group(2));
	}

	@Test
	public void testParseQueryString() {
		final String queryString = "auth_key=abc123&toto=titi";

		final Map<String, String> result = parseQueryString(queryString);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("abc123", result.get("auth_key"));
		Assert.assertEquals("titi", result.get("toto"));
	}
}
