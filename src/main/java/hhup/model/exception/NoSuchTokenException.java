package hhup.model.exception;

import java.util.UUID;

public class NoSuchTokenException extends Exception {

	private static final long serialVersionUID = -9042198678294651811L;
	private UUID token;

	public NoSuchTokenException(UUID token) {
		this.token = token;
	}

	public NoSuchTokenException(@SuppressWarnings("unused") String key) {
		// TODO Auto-generated constructor stub
	}

	public UUID getToken() {
		return token;
	}
}