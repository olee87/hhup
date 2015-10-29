package hhup.model.exception;

import java.util.UUID;

public class SessionNotFoundException extends Exception {

	private static final long serialVersionUID = 174134671145884360L;
	private UUID sessionId;

	public SessionNotFoundException(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	@Override
	public String getMessage() {
		return "no session found with id " + sessionId;
	}
}