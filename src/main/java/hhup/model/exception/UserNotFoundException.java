package hhup.model.exception;

import java.util.UUID;

public class UserNotFoundException extends Exception {

	private static final long serialVersionUID = 3146092231237897865L;
	private UUID userId;
	private String username;

	public UserNotFoundException(UUID userId) {
		this.userId = userId;
		this.username = null;
	}

	public UserNotFoundException(String username) {
		this.username = username;
	}

	public UUID getUserId() {
		return userId;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("no user found");
		if (userId != null) {
			sb.append(" with userId ").append(userId);
		}
		if (username != null) {
			sb.append(" with username ").append(userId);
		}
		return sb.append(".").toString();
	}
}