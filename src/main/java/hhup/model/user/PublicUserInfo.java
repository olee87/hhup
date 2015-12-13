package hhup.model.user;

import java.util.EnumSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Contains the info about a user that can be made visible publicly.
 */
@JsonInclude(Include.NON_NULL)
public class PublicUserInfo extends UserInfo {

	private DateTime registrationDate;
	private DateTime checkinDate;
	private DateTime payDate;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private GoogleLocation homeId;
	private String homeString;
	private String nationality;
	private String csProfile;
	private String hcProfile;
	private String bwProfile;
	private String fbProfile;
	private UUID id;
	private Boolean activated;
	private Boolean checkedIn;
	private Boolean paid;
	private Boolean hideLastName;
	private EnumSet<Authority> authorities;
	private String phone;
	private String[] languages;

	public PublicUserInfo(DateTime registrationDate, DateTime checkinDate, DateTime payDate, String username,
			String firstName, String lastName, String email, GoogleLocation homeId, String homeString, String nationality,
			String csProfile, String hcProfile, String bwProfile, String fbProfile, UUID id, Boolean activated,
			Boolean checkedIn, Boolean paid, Boolean hideLastName, EnumSet<Authority> authorities, String phone, String[] languages) {
		this.registrationDate = registrationDate;
		this.checkinDate = checkinDate;
		this.payDate = payDate;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.homeId = homeId;
		this.homeString = homeString;
		this.nationality = nationality;
		this.csProfile = csProfile;
		this.hcProfile = hcProfile;
		this.bwProfile = bwProfile;
		this.fbProfile = fbProfile;
		this.id = id;
		this.activated = activated;
		this.checkedIn = checkedIn;
		this.paid = paid;
		this.hideLastName = hideLastName;
		this.authorities = authorities;
		this.phone = phone;
		this.languages = languages;
	}

	public DateTime getRegistrationDate() {
		return registrationDate;
	}

	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public GoogleLocation getHomeId() {
		return homeId;
	}

	public String getHomeString() {
		return homeString;
	}

	public String getNationality() {
		return nationality;
	}

	public String getCsProfile() {
		return csProfile;
	}

	public String getHcProfile() {
		return hcProfile;
	}

	public String getBwProfile() {
		return bwProfile;
	}

	public String getFbProfile() {
		return fbProfile;
	}

	@Override
	public UUID getId() {
		return id;
	}

	public Boolean isActivated() {
		return activated;
	}

	public Boolean isPaid() {
		return paid;
	}

	public Boolean isHideLastName() {
		return hideLastName;
	}

	public EnumSet<Authority> getAuthorities() {
		return authorities;
	}

	public String getPhone() {
		return phone;
	}

	public String[] getLanguages() {
		return languages;
	}

	public Boolean isCheckedIn() {
		return checkedIn;
	}

	public DateTime getCheckinDate() {
		return checkinDate;
	}

	public DateTime getPayDate() {
		return payDate;
	}

	public String getRealName() {
		StringBuilder name = new StringBuilder();
		name.append(StringUtils.defaultIfEmpty(firstName, "-"));
		if ((hideLastName != null) && hideLastName) {
			name.append(" ").append(StringUtils.defaultIfEmpty(lastName, "-"));
		}
		return name.toString();
	}
}