package hhup.web.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CouchMarketPostRequest {

	@JsonProperty(required = false)
	private String type;

	@JsonProperty(required = false)
	private String text;

	@JsonProperty(required = false)
	private Integer count;

	@JsonProperty(required = false)
	private UUID id;

	public String getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public Integer getCount() {
		return count;
	}

	public UUID getId() {
		return id;
	}
}