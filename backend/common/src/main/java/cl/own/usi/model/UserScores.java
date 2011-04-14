package cl.own.usi.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class UserScores {
	List<String> mails = new ArrayList<String>();
	List<Integer> scores = new ArrayList<Integer>();
	List<String> firstnames = new ArrayList<String>();
	List<String> lastnames = new ArrayList<String>();

	@JsonProperty(value = "mail")
	public List<String> getMails() {
		return mails;
	}

	@JsonProperty(value = "scores")
	public List<Integer> getScores() {
		return scores;
	}

	@JsonProperty(value = "firstname")
	public List<String> getFirstnames() {
		return firstnames;
	}

	@JsonProperty(value = "lastname")
	public List<String> getLastnames() {
		return lastnames;
	}

	public void append(String email, Integer score, String firstname,
			String lastname) {
		mails.add(email);
		scores.add(score);
		firstnames.add(firstname);
		lastnames.add(lastname);
	}

}
