package hhup.model.user;

import java.util.UUID;

public abstract class UserInfo implements Comparable<UserInfo> {

	public abstract UUID getId();

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof UserInfo) { return ((UserInfo) o).getId().equals(getId()); }
		return false;
	}

	@Override
	public int compareTo(UserInfo other) {
		return getId().compareTo(other.getId());
	}
}