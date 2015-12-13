package hhup.model.user;

import java.util.EnumSet;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InternalUserInfo extends UserInfo {

	private DateTime registrationDate;
	private DateTime checkinDate;
	private DateTime payDate;
	private String username;
	private String firstName;
	private String lastName;
	private String password;
	private String email;
	private GoogleLocation homeId;
	private String homeString;
	private String nationality;
	private String csProfile;
	private String hcProfile;
	private String bwProfile;
	private String fbProfile;
	private String phone;
	private String[] languages;
	private UUID id;
	private Boolean activated;
	private Boolean checkedIn;
	private Boolean hideLastName;
	private Boolean paid;
	private EnumSet<Authority> authorities;

	public InternalUserInfo() { /* default constructor for jackson */}

	public InternalUserInfo(String username, String password, String firstName, String lastName, String email, GoogleLocation homeId,
			String homeString, String nationality, String csProfile, String hcProfile, String bwProfile,
			String fbProfile, UUID id, Boolean activated, Boolean checkedIn, Boolean paid, Boolean hideLastName,
			EnumSet<Authority> authorities, String phone, String[] languages, DateTime registrationDate,
			DateTime checkinDate, DateTime payDate) {
		super();
		this.username = username;
		this.password = password;
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
		this.authorities = authorities;
		this.phone = phone;
		this.languages = languages;
		this.registrationDate = registrationDate;
		this.checkinDate = checkinDate;
		this.payDate = payDate;
		this.hideLastName = hideLastName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
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

	public Boolean isCheckedIn() {
		return checkedIn;
	}

	public Boolean isPaid() {
		return paid;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public Boolean isHideLastName() {
		return hideLastName;
	}

	public EnumSet<Authority> getAuthorities() {
		return authorities;
	}

	public DateTime getRegistrationDate() {
		return registrationDate;
	}

	public String getPhone() {
		return phone;
	}

	public String[] getLanguages() {
		return languages;
	}

	public DateTime getCheckinDate() {
		return checkinDate;
	}

	public DateTime getPayDate() {
		return payDate;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("USER[");

		if (id != null) {
			builder.append("id: ").append(id).append(", ");
		}

		builder.append("username: ").append(username).append(", email: ").append(email);
		builder.append(", real name: ").append(firstName).append(" ").append(lastName);

		if (homeId != null) {
			builder.append(", home id (google): ").append(homeId.getId()).append(", readable home name: ")
					.append(homeId.getReadable());
		}

		if (homeString != null) {
			builder.append(", home: ").append(homeString);
		}

		if (nationality != null) {
			builder.append(", nationality: ").append(nationality);
		}

		if (csProfile != null) {
			builder.append(", cs: ").append(csProfile);
		}

		if (hcProfile != null) {
			builder.append(", hc: ").append(hcProfile);
		}

		if (bwProfile != null) {
			builder.append(", bw: ").append(bwProfile);
		}

		if (fbProfile != null) {
			builder.append(", fb: ").append(fbProfile);
		}

		if (activated != null) {
			builder.append(", activated: ").append(activated);
		}

		if (activated != null) {
			builder.append(", checked in: ").append(checkedIn);
		}

		if (authorities != null) {
			builder.append(", authorities: ").append(authorities);
		}

		builder.append("]");

		return builder.toString();
	}
}