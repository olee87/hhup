package hhup.web.request;

import java.util.UUID;

public class MakeAdminRequest {

	private UUID userId;
	private boolean makeAdmin;

	public UUID getUserId() {
		return userId;
	}

	public boolean isMakeAdmin() {
		return makeAdmin;
	}
}