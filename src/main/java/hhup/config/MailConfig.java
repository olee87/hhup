package hhup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="hhup.mail")
public class MailConfig {

	private String host;
	private int port;
	private String username;
	private String password;
	private String fromAddress;
	private String protocol;

	private boolean simulate = false;

	public String getProtocol() {
		return protocol;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public boolean isSimulate() {
		return simulate;
	}
}