package hhup.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailOrUsernameRequest {

	@JsonProperty(required = false)
	private String email;

	@JsonProperty(required = false)
	private String username;

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}
}