package cl.own.usi.gateway.client.impl.thrift;

import org.apache.thrift.TException;

import cl.own.usi.thrift.WorkerRPC.Client;

public class GetAnswersAsJsonAction implements ThriftRetryableAction<String> {

	private String email;
	private Integer questionNumber;

	public GetAnswersAsJsonAction(final String email,
			final Integer questionNumber) {
		super();
		this.email = email;
		this.questionNumber = questionNumber;
	}

	@Override
	public String doAction(final Client client) throws TException {

		if (questionNumber == null) {
			return client.getAllAnswersAsJson(email);
		} else {
			return client.getAnswerAsJson(email, questionNumber);
		}
	}

}
