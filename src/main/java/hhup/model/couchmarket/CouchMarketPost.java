package hhup.model.couchmarket;

import java.util.UUID;

import org.joda.time.DateTime;

public class CouchMarketPost implements Comparable<CouchMarketPost> {

	public enum Type {
		OFFER, SEARCH
	}

	private Type type;
	private String text;
	private UUID userId;
	private Integer count;
	private DateTime creationDate;
	private UUID id;

	public CouchMarketPost() {
		/* for jackson */
	}

	public CouchMarketPost(Type type, String text, UUID userId, Integer count, DateTime creationDate, UUID id) {
		this.type = type;
		this.text = text;
		this.userId = userId;
		this.count = count;
		this.creationDate = creationDate;
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public UUID getUserId() {
		return userId;
	}

	public Integer getCount() {
		return count;
	}

	public DateTime getCreationDate() {
		return creationDate;
	}

	public UUID getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CouchMarketPost) { return ((CouchMarketPost) obj).getId().equals(id); }
		return false;
	}

	@Override
	public int compareTo(CouchMarketPost other) {
		return id.compareTo(other.getId());
	}
}