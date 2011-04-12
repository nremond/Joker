package cl.own.usi.json;

import org.codehaus.jackson.annotate.JsonProperty;

public class UserRequest {

	private String firstname;
	private String lastname;
	private String mail;
	private String password;

	@JsonProperty(value = "firstname")
	public String getFirstname() {
		return firstname;
	}

	@JsonProperty(value = "firstname")
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	@JsonProperty(value = "lastname")
	public String getLastname() {
		return lastname;
	}

	@JsonProperty(value = "lastname")
	public void setLastname(String lastname) {
		this.lastname = lastname;
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
