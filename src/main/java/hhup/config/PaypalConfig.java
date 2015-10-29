package hhup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="hhup.paypal")
public class PaypalConfig {

	private double costPerUser;
	private String callbackUrl;
	private boolean sandbox;
	private String receiverEmail;

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public double getCostPerUser() {
		return costPerUser;
	}

	public String getReceiverEmail() {
		return receiverEmail;
	}

	public boolean isSandbox() {
		return sandbox;
	}
}