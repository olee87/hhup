package hhup.model.token;

import java.util.UUID;

public class AccountActivationToken implements Comparable<AccountActivationToken> {

	private UUID userId;
	private UUID value;

	public AccountActivationToken() { /* for jackson */}

	public AccountActivationToken(UUID userId, UUID code) {
		super();
		this.userId = userId;
		this.value = code;
	}

	public UUID getValue() {
		return value;
	}

	public UUID getUserId() {
		return userId;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AccountActivationToken) { return ((AccountActivationToken) o).getValue().equals(getValue()); }
		return false;
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}

	@Override
	public int compareTo(AccountActivationToken other) {
		return getValue().compareTo(other.getValue());
	}
}