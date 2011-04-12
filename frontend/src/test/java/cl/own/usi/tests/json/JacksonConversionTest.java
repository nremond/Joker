package cl.own.usi.tests.json;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import cl.own.usi.json.AnswerRequest;
import cl.own.usi.json.AnswerResponse;
import cl.own.usi.json.UserRequest;

public class JacksonConversionTest {
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testAnswerConversion() throws Exception {

		final String result = mapper.writeValueAsString(new AnswerResponse(
				false, "1", 10));

		Assert.assertEquals("AnswerResponse as json",
				"{\"are_u_right\":false,\"good_answer\":\"1\",\"score\":10}",
				result);

		final String answerInput = "{\"answer\":42}";

		final AnswerRequest request = mapper.readValue(answerInput,
				AnswerRequest.class);

		Assert.assertNotNull("AnswerResponse object should not be null",
				request);
		Assert.assertEquals(Integer.valueOf(42), request.getAnswer());
	}

	@Test
	public void testUserConversion() throws Exception {
		final String request = "{ \"firstname\" : \"Bob\", \"lastname\" : \"Morane\", \"mail\" : \"bob.morane@gmail.com\", \"password\" : \"toto\" }";

		final UserRequest userRequest = mapper.readValue(request,
				UserRequest.class);

		Assert.assertNotNull(userRequest);
		Assert.assertEquals("Bob", userRequest.getFirstname());
		Assert.assertEquals("Morane", userRequest.getLastname());
		Assert.assertEquals("bob.morane@gmail.com", userRequest.getMail());
		Assert.assertEquals("toto", userRequest.getPassword());
	}
}
