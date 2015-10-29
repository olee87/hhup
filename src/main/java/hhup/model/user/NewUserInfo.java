package hhup.model.user;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewUserInfo extends UserInfo {

	private String username;
	private String password;
	private String email;

	@JsonProperty(required = false)
	private String realName;

	@JsonProperty(required = false)
	private String phone;

	@JsonProperty(required = false)
	private String[] languages;

	@JsonProperty(required = false)
	private GoogleLocation homeId;

	@JsonProperty(required = false)
	private String homeString;

	@JsonProperty(required = false)
	private String nationality;

	@JsonProperty(required = false)
	private String csProfile;

	@JsonProperty(required = false)
	private String hcProfile;

	@JsonProperty(required = false)
	private String bwProfile;

	@JsonProperty(required = false)
	private String fbProfile;

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public String getRealName() {
		return realName;
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

	public String[] getLanguages() {
		return languages;
	}

	public String getPhone() {
		return phone;
	}

	@Override
	public UUID getId() {
		return UUID.nameUUIDFromBytes(username.getBytes());
	}
}
