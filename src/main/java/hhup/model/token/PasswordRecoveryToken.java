package hhup.model.token;

import hhup.model.user.InternalUserInfo;

import java.util.UUID;

import org.joda.time.DateTime;

public class PasswordRecoveryToken {

	private DateTime requestTime;
	private UUID token;
	private UUID userId;

	public PasswordRecoveryToken() { /* for jackson */}

	public PasswordRecoveryToken(InternalUserInfo user, UUID token) {
		requestTime = DateTime.now();
		userId = user.getId();
		this.token = token;
	}

	public DateTime getRequestTime() {
		return requestTime;
	}

	public UUID getToken() {
		return token;
	}

	public UUID getUserId() {
		return userId;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PasswordRecoveryToken) { return ((PasswordRecoveryToken) o).getUserId().equals(getUserId()); }
		return false;
	}

	@Override
	public int hashCode() {
		return getUserId().hashCode();
	}

	public int compareTo(PasswordRecoveryToken other) {
		return getUserId().compareTo(other.getUserId());
	}
}