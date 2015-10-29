package hhup.model.user;

import java.util.UUID;

import org.joda.time.DateTime;

public class Note {

	private String text;
	private UUID authorId;
	private UUID id;
	private UUID userId;
	private DateTime creationDate;

	public Note() { /* for jackson */}

	public Note(String text, UUID authorId, UUID id, UUID userId, DateTime creationDate) {
		super();
		this.text = text;
		this.authorId = authorId;
		this.id = id;
		this.creationDate = creationDate;
		this.userId = userId;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAuthorId() {
		return authorId;
	}

	public DateTime getCreationDate() {
		return creationDate;
	}

	public String getText() {
		return text;
	}

	public UUID getUserId() {
		return userId;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Note) ? ((Note) obj).getId().equals(id) : false;
	}
}