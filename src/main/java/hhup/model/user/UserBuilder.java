package hhup.model.user;

import java.util.EnumSet;
import java.util.UUID;

import org.joda.time.DateTime;

public class UserBuilder {

	private String username;
	private String realName;
	private String password;
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
	private EnumSet<Authority> authorities;
	private DateTime registrationDate;
	private DateTime payDate;
	private DateTime checkinDate;
	private String phone;
	private String[] languages;

	private UserBuilder() {/* only managed instances */}

	public static UserBuilder blankUser() {
		UserBuilder builder = new UserBuilder();
		builder.username = null;
		builder.realName = null;
		builder.password = null;
		builder.email = null;
		builder.homeId = null;
		builder.homeString = null;
		builder.nationality = null;
		builder.csProfile = null;
		builder.hcProfile = null;
		builder.bwProfile = null;
		builder.fbProfile = null;
		builder.id = null;
		builder.activated = null;
		builder.checkedIn = null;
		builder.paid = null;
		builder.registrationDate = null;
		builder.checkinDate = null;
		builder.payDate = null;
		builder.authorities = EnumSet.noneOf(Authority.class);
		builder.phone = null;
		builder.languages = null;
		return builder;
	}

	public static UserBuilder from(NewUserInfo user) {
		UserBuilder builder = new UserBuilder();
		builder.username = user.getUsername();
		builder.realName = user.getRealName();
		builder.password = user.getPassword();
		builder.email = user.getEmail();
		builder.homeId = user.getHomeId();
		builder.homeString = user.getHomeString();
		builder.nationality = user.getNationality();
		builder.csProfile = user.getCsProfile();
		builder.hcProfile = user.getHcProfile();
		builder.bwProfile = user.getBwProfile();
		builder.fbProfile = user.getFbProfile();
		builder.phone = user.getPhone();
		builder.languages = user.getLanguages();
		builder.activated = false;
		builder.checkedIn = false;
		builder.paid = false;
		builder.registrationDate = DateTime.now();
		builder.checkinDate = null;
		builder.payDate = null;
		builder.authorities = EnumSet.noneOf(Authority.class);
		return builder;
	}

	public static UserBuilder from(InternalUserInfo user) {
		UserBuilder builder = new UserBuilder();
		builder.username = user.getUsername();
		builder.realName = user.getRealName();
		builder.password = user.getPassword();
		builder.email = user.getEmail();
		builder.homeId = user.getHomeId();
		builder.homeString = user.getHomeString();
		builder.nationality = user.getNationality();
		builder.csProfile = user.getCsProfile();
		builder.hcProfile = user.getHcProfile();
		builder.bwProfile = user.getBwProfile();
		builder.fbProfile = user.getFbProfile();
		builder.id = user.getId();
		builder.activated = user.isActivated();
		builder.checkedIn = user.isCheckedIn();
		builder.paid = user.isPaid();
		builder.authorities = user.getAuthorities();
		builder.phone = user.getPhone();
		builder.languages = user.getLanguages();
		builder.registrationDate = user.getRegistrationDate();
		builder.checkinDate = user.getCheckinDate();
		builder.payDate = user.getPayDate();
		return builder;
	}

	public UserBuilder withUsername(String username) {
		this.username = username;
		return this;
	}

	public UserBuilder withRealName(String realName) {
		this.realName = realName;
		return this;
	}

	public UserBuilder withPassword(String password) {
		this.password = password;
		return this;
	}

	public UserBuilder withEmail(String email) {
		this.email = email;
		return this;
	}

	public UserBuilder withHomeId(GoogleLocation homeId) {
		this.homeId = homeId;
		return this;
	}

	public UserBuilder withHomeString(String homeString) {
		this.homeString = homeString;
		return this;
	}

	public UserBuilder withNationality(String nationality) {
		this.nationality = nationality;
		return this;
	}

	public UserBuilder withCsProfile(String csProfile) {
		this.csProfile = csProfile;
		return this;
	}

	public UserBuilder withBwProfile(String bwProfile) {
		this.bwProfile = bwProfile;
		return this;
	}

	public UserBuilder withHcProfile(String hcProfile) {
		this.hcProfile = hcProfile;
		return this;
	}

	public UserBuilder withFbProfile(String fbProfile) {
		this.fbProfile = fbProfile;
		return this;
	}

	public UserBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public UserBuilder withActivated(Boolean activated) {
		this.activated = activated;
		return this;
	}

	public UserBuilder withCheckedIn(Boolean checkedIn) {
		this.checkedIn = checkedIn;
		return this;
	}

	public UserBuilder withPaid(Boolean paid) {
		this.paid = paid;
		return this;
	}

	public UserBuilder withAuthorities(EnumSet<Authority> authorities) {
		this.authorities = authorities;
		return this;
	}

	public UserBuilder withAdditionalAuthority(Authority authority) {
		authorities.add(authority);
		return this;
	}

	public UserBuilder withoutAuthority(Authority authority) {
		authorities.remove(authority);
		return this;
	}

	public UserBuilder withRegistrationDate(DateTime registrationDate) {
		this.registrationDate = registrationDate;
		return this;
	}

	public UserBuilder withCheckinDate(DateTime checkinDate) {
		this.checkinDate = checkinDate;
		return this;
	}

	public UserBuilder withPayDate(DateTime payDate) {
		this.payDate = payDate;
		return this;
	}

	public void withPhone(String phone) {
		this.phone = phone;
	}

	public void withLanguages(String[] languages) {
		this.languages = languages;
	}

	public InternalUserInfo asInternalUserInfo() {
		return new InternalUserInfo(username, password, realName, email, homeId, homeString, nationality, csProfile,
				hcProfile, bwProfile, fbProfile, id, activated, checkedIn, paid, authorities, phone, languages,
				registrationDate, checkinDate, payDate);
	}

	public PublicUserInfo asPublicUserInfo() {
		return new PublicUserInfo(registrationDate, checkinDate, payDate, username, realName, email, homeId,
				homeString, nationality, csProfile, hcProfile, bwProfile, fbProfile, id, activated, checkedIn, paid,
				authorities, phone, languages);
	}
}