package cl.own.usi.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Object that hold the answer to the audit request for all answers of a given
 * user.
 *
 * @author reynald
 *
 */
public class AuditAnswers {

	private List<Integer> userAnswers;
	private List<Integer> goodAnswers;

	public AuditAnswers(final List<Integer> goodAnswers) {
		super();
		this.goodAnswers = goodAnswers;

		this.userAnswers = new ArrayList<Integer>(goodAnswers.size());
	}

	@JsonProperty(value = "user_answers")
	public List<Integer> getUserAnswers() {
		return userAnswers;
	}

	@JsonProperty(value = "good_answers")
	public List<Integer> getGoodAnswers() {
		return goodAnswers;
	}

}
