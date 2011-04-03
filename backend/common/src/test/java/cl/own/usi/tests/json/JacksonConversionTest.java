package cl.own.usi.tests.json;

import java.util.Arrays;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import cl.own.usi.model.AuditAnswer;
import cl.own.usi.model.AuditAnswers;

public class JacksonConversionTest {
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testAuditAnswersConversion() throws Exception {

		final AuditAnswers auditAnswers = new AuditAnswers(Arrays.asList(1, 2,
				1, 3, 4));
		auditAnswers.getUserAnswers().add(1);
		auditAnswers.getUserAnswers().add(5);
		auditAnswers.getUserAnswers().add(3);
		auditAnswers.getUserAnswers().add(4);
		auditAnswers.getUserAnswers().add(2);

		final String result = mapper.writeValueAsString(auditAnswers);

		Assert.assertEquals("AuditAnswers as json",
				"{\"user_answers\":[1,5,3,4,2],\"good_answers\":[1,2,1,3,4]}",
				result);
	}

	@Test
	public void testAuditAnswerConversion() throws Exception {
		final String question = "Dummy question?";
		final AuditAnswer answer = new AuditAnswer(2, 4, question);

		final String result = mapper.writeValueAsString(answer);

		Assert.assertEquals("AuditAnswer as json",
				"{\"user_answer\":2,\"good_answer\":4,\"question\":\""
						+ question + "\"}", result);

	}
}
