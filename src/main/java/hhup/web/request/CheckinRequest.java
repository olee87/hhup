package hhup.web.request;

import java.util.UUID;

public class CheckinRequest {

	private UUID userId;
	private Boolean checkin;

	public UUID getUserId() {
		return userId;
	}

	public Boolean isCheckin() {
		return checkin;
	}
}