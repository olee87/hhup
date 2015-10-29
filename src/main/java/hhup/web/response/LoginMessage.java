package hhup.web.response;

public class LoginMessage {

	private boolean active;
	private String message;

	public LoginMessage(boolean active, String message) {
		this.active = active;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public boolean isActive() {
		return active;
	}
}