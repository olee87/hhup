package hhup.model.exception;

import java.util.UUID;

public class UnauthorizedException extends Exception {

	private static final long serialVersionUID = 8897353113107468639L;
	private UUID userId;

	public UnauthorizedException(UUID userId) {
		this.userId = userId;
	}

	public UnauthorizedException() {
		// TODO Auto-generated constructor stub
	}

	public UUID getRequesterId() {
		return userId;
	}

	@Override
	public String getMessage() {
		return "user with id " + userId + " is not authorized to perform the requested action.";
	}
}