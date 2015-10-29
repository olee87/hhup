package hhup.web.request;

import java.util.UUID;


public class CreateNoteRequest {

	private UUID userId;
	private String text;

	public String getText() {
		return text;
	}

	public UUID getUserId() {
		return userId;
	}
}