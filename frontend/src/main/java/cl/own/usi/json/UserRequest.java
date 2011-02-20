package cl.own.usi.json;

import org.codehaus.jackson.annotate.JsonProperty;

public class UserRequest {

	private String firstName;
	private String lastName;
	private String mail;
	private String password;

	@JsonProperty(value = "firstname")
	public String getFirstName() {
		return firstName;
	}

	@JsonProperty(value = "firstname")
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@JsonProperty(value = "lastname")
	public String getLastName() {
		return lastName;
	}

	@JsonProperty(value = "lastname")
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
