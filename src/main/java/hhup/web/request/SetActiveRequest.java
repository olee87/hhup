package hhup.web.request;

import java.util.UUID;

public class SetActiveRequest {

	private UUID userId;
	private Boolean active;

	public Boolean isActive() {
		return active;
	}

	public UUID getUserId() {
		return userId;
	}
}