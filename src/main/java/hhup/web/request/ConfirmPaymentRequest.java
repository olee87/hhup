package hhup.web.request;

import java.util.UUID;

public class ConfirmPaymentRequest {

	private UUID userId;
	private Boolean paid;

	public UUID getUserId() {
		return userId;
	}

	public Boolean isPaid() {
		return paid;
	}
}