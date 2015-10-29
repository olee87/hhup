package hhup.web.request;

import hhup.model.user.GoogleLocation;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EditUserRequest {

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

	@JsonProperty(required = false)
	private UUID userId;

	@JsonProperty(required = false)
	private String phone;

	@JsonProperty(required = false)
	private String[] languages;

	@JsonProperty(required = false)
	private String realName;

	public String getBwProfile() {
		return bwProfile;
	}

	public String getCsProfile() {
		return csProfile;
	}

	public String getFbProfile() {
		return fbProfile;
	}

	public String getHcProfile() {
		return hcProfile;
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

	public UUID getUserId() {
		return userId;
	}

	public String[] getLanguages() {
		return languages;
	}

	public String getPhone() {
		return phone;
	}

	public String getRealName() {
		return realName;
	}
}