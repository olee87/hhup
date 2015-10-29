package hhup.web.request;

import java.util.UUID;

public class RecoverPasswordRequest {

	private UUID token;
	private String password;

	public String getPassword() {
		return password;
	}

	public UUID getToken() {
		return token;
	}
}