package hhup.model.guestbook;

import java.util.UUID;

import org.joda.time.DateTime;

public class GuestbookEntry {

	private UUID id;
	private UUID authorId;
	private String message;
	private DateTime creationTime;

	public GuestbookEntry(UUID authorId, String message, DateTime creationTime) {
		this.authorId = authorId;
		this.message = message;
		this.creationTime = creationTime;
		this.id = UUID.randomUUID();
	}

	public GuestbookEntry() { /* for jackson */ }

	public UUID getAuthorId() {
		return authorId;
	}

	public String getMessage() {
		return message;
	}

	public DateTime getCreationTime() {
		return creationTime;
	}

	public UUID getId() {
		return id;
	}
}