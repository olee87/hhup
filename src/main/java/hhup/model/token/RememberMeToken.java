package hhup.model.token;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * these tokens are used by Spring Security to automatically log in users.
 *
 * @see hhup.service.RememberMeService.myapp.security.CustomPersistentRememberMeServices
 */

public class RememberMeToken {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("d MMMM yyyy");

	private static final int MAX_USER_AGENT_LEN = 255;

	private String series;

	private String tokenValue;

	private LocalDate tokenDate;

	private String ipAddress;

	private String userAgent;

	private UUID userId;

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getTokenValue() {
		return tokenValue;
	}

	public void setTokenValue(String tokenValue) {
		this.tokenValue = tokenValue;
	}

	public LocalDate getTokenDate() {
		return tokenDate;
	}

	public void setTokenDate(LocalDate tokenDate) {
		this.tokenDate = tokenDate;
	}

	@JsonIgnore
	public String getFormattedTokenDate() {
		return DATE_TIME_FORMATTER.print(this.tokenDate);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		if (userAgent.length() >= MAX_USER_AGENT_LEN) {
			this.userAgent = userAgent.substring(0, MAX_USER_AGENT_LEN - 1);
		} else {
			this.userAgent = userAgent;
		}
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if ((o == null) || (getClass() != o.getClass())) { return false; }

		RememberMeToken that = (RememberMeToken) o;

		if (!series.equals(that.series)) { return false; }

		return true;
	}

	@Override
	public int hashCode() {
		return series.hashCode();
	}

	public int compareTo(RememberMeToken other) {
		return series.compareTo(other.getSeries());
	}

	@Override
	public String toString() {
		return "PersistentToken{" + "series='" + series + '\'' + ", tokenValue='" + tokenValue + '\'' + ", tokenDate="
				+ tokenDate + ", ipAddress='" + ipAddress + '\'' + ", userAgent='" + userAgent + '\'' + "}";
	}
}