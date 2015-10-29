package hhup.model.exception;

import java.util.UUID;

public class PostNotFoundException extends Exception {

	private UUID id;

	public PostNotFoundException(UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}

	private static final long serialVersionUID = 3050835879709185975L;

}