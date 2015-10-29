package hhup.model.history;

import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

public class HistoryItem {

	private HistoryType type;
	private UUID itemId;
	private UUID actorId;
	private Set<UUID> subjectIds;
	private String text;
	private DateTime date;

	public HistoryItem() { /* for jackson */ }

	public HistoryItem(HistoryType type, UUID actorId, Set<UUID> subjectIds, String text, DateTime date) {
		this.type = type;
		this.actorId = actorId;
		this.subjectIds = subjectIds;
		this.text = text;
		this.date = date;
		this.itemId = UUID.randomUUID();
	}

	public HistoryType getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public UUID getActorId() {
		return actorId;
	}

	public Set<UUID> getSubjectIds() {
		return subjectIds;
	}

	public UUID getItemId() {
		return itemId;
	}

	public DateTime getDate() {
		return date;
	}
}