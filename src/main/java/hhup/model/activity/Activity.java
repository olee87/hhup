package hhup.model.activity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Activity implements Comparable<Activity> {

	@JsonProperty(required = false)
	private UUID id;
	@JsonProperty(required = false)
	private DateTime startDate;
	@JsonProperty(required = false)
	private DateTime endDate;
	private Location location;
	private Set<UUID> contact;
	private String title;
	private String description;
	@JsonProperty(required = false)
	private Set<UUID> participants;
	@JsonProperty(required = false)
	private Integer limit;
	private Boolean limited;
	@JsonProperty(required = false)
	private List<Image> images;
	@JsonProperty(required = false)
	private Boolean fullSize;
	@JsonProperty(required = false)
	private Boolean canCollide;

	public DateTime getStartDate() {
		return startDate;
	}

	public DateTime getEndDate() {
		return endDate;
	}

	public Location getLocation() {
		return location;
	}

	public Set<UUID> getContact() {
		return contact;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public UUID getId() {
		if (id == null) {
			return UUID.fromString("00000000-0000-0000-0000-000000000000");
		}
		return id;
	}

	public Set<UUID> getParticipants() {
		return participants;
	}

	public Integer getLimit() {
		return limit;
	}

	public Boolean isLimited() {
		return limited;
	}

	public void setParticipants(Set<UUID> participants) {
		this.participants = participants;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public boolean addParticipant(UUID id) {
		return participants.add(id);
	}

	public boolean removeParticipant(UUID id) {
		return participants.remove(id);
	}

	public List<Image> getImages() {
		return images;
	}

	public Boolean isCanCollide() {
		return canCollide != null ? canCollide : Boolean.FALSE;
	}

	public Boolean isFullSize() {
		return fullSize;
	}

	public boolean overlapsWith(Activity other) {
		Interval thisInterval = new Interval(getStartDate(), getEndDate());
		Interval otherInterval = new Interval(other.getStartDate(), other.getEndDate());
		return thisInterval.overlaps(otherInterval);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Activity) { return ((Activity) o).getId().equals(getId()); }
		return false;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public int compareTo(Activity other) {
		return getId().compareTo(other.getId());
	}
}