package hhup.web.request;

import java.util.UUID;


public class MassMailRequest {

	private UUID activityId;
	private String text;
	private String subject;

	public UUID getActivityId() {
		return activityId;
	}

	public String getSubject() {
		return subject;
	}

	public String getText() {
		return text;
	}
}