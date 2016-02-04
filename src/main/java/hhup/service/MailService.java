package hhup.service;

import hhup.config.MailConfig;
import hhup.model.exception.UserNotFoundException;
import hhup.model.user.InternalUserInfo;
import hhup.web.request.PaypalPaymentInfo;

import java.io.StringWriter;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Service
public class MailService {

	private static final String ACTIVITIES_PATH = "/#/activities/";
	private static final String CONFIRM_PATH = "/#/confirm/";
	private static final String RECOVER_PATH = "/#/recoverPassword/";

	private static final String SUBJECT_START = "[Hamburg CS Invasion 2016] ";

	private Logger log = LoggerFactory.getLogger(MailService.class);

	@Value("${hhup.ownHostName}")
	private String ownHostName;

	@Autowired
	private MailConfig config;

	@Autowired
	private UserService users;

	@Autowired
	private ActivitiesService activities;

	private JavaMailSenderImpl sender;

	private VelocityEngine engine;

	private String hhupAddress;

	@PostConstruct
	public void init() {
		hhupAddress = ownHostName;
		if (!hhupAddress.startsWith("http")) {
			hhupAddress = "http://" + hhupAddress;
		}
		sender = new JavaMailSenderImpl();
		sender.setHost(config.getHost());
		sender.setPort(config.getPort());
		sender.setProtocol(config.getProtocol());
		if ((config.getUsername() != null) && (config.getPassword() != null)) {
			sender.setUsername(config.getUsername());
			sender.setPassword(config.getPassword());
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.quitwait", "false");

			sender.setJavaMailProperties(props);
		}
		engine = new VelocityEngine();
		engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
		engine.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		engine.init();
	}

	public void sendRegistrationConfirmation(UUID token, InternalUserInfo user) throws MessagingException {
		if (config.isSimulate()) {
			log.info("would send registration email to {} with token {}.", user.getEmail(), token);
		} else {
			VelocityContext context = new VelocityContext();

			String name = StringUtils.defaultIfBlank(user.getFirstName(), user.getUsername());
			String link = hhupAddress + CONFIRM_PATH + token.toString();

			context.put("name", name);
			context.put("link", link);

			String text = "Hi " + name + ", thank you for signing up for the Hamburg CouchSurfing Invasion 2015!"
					+ " To confirm your registration please click or paste this link into your browser: " + link;

			String subject = SUBJECT_START + "confirm your registration";
			String template = "/templates/registerConfirm.vt";

			sendMail(subject, template, text, context, user.getEmail());
		}
	}

	public void sendPasswordRecoveryMail(InternalUserInfo user, UUID token) throws MessagingException {
		if (config.isSimulate()) {
			log.info("would send password recovery email to {} with token {}", user.getEmail(), token);
		} else {
			log.info("sending password recovery email to {}", user.getEmail(), token);
			VelocityContext context = new VelocityContext();

			String name = StringUtils.defaultIfBlank(user.getFirstName(), user.getUsername());
			String link = hhupAddress + RECOVER_PATH + token.toString();

			context.put("name", name);
			context.put("link", link);

			String text = "Hi " + name + ", to recover your password for the Hamburg CouchSurfing Invasion website"
					+ " please click or paste this link into your browser: " + link;

			String subject = SUBJECT_START + "recover password";
			String template = "/templates/recoverPassword.vt";

			sendMail(subject, template, text, context, user.getEmail());
		}
	}

	public void sendTestMail(String address) throws MessagingException {
		log.warn("sending test mail to {}", address);
		sendMail("test", "/templates/test.vt", "test mail", new VelocityContext(), address);
	}

	public void sendGenericMail(String text, String subject, String... addresses) throws MessagingException {
		VelocityContext context = new VelocityContext();
		context.put("text", text);
		sendMail(subject, "/templates/generic.vt", text, context, addresses);
	}

	private void sendMail(String subject, String templateName, String text, VelocityContext context, String... addresses)
			throws MailException, ResourceNotFoundException, ParseErrorException, MessagingException {

		MimeMessage message = sender.createMimeMessage();
		try {
			message.setFrom(new InternetAddress(config.getFromAddress()));

			Template template = engine.getTemplate(templateName);

			StringWriter sw = new StringWriter();

			template.merge(context, sw);

			message.setText(text);
			message.setContent(sw.toString(), "text/html");
			message.setRecipients(RecipientType.TO, toAddresses(addresses));
			message.setSubject(subject);
			sender.send(message);
		} catch (MailException | ResourceNotFoundException | ParseErrorException | MessagingException e) {
			log.warn(e.getMessage());
			throw e;
		}
	}

	private Address[] toAddresses(String[] addressStrings) {
		Set<Address> addresses = Sets.newHashSet();
		for (String address : addressStrings) {
			try {
				addresses.add(new InternetAddress(address));
			} catch (AddressException e) { /* do nothing */ }
		}
		return Iterables.toArray(addresses, Address.class);
	}

	public void sendPaypalConfirmedMail(PaypalPaymentInfo info) {

		try {
			UUID payingUserId = info.getUserInfo().getPayingUser();
			InternalUserInfo payingUser = users.getUserForId(payingUserId);

			VelocityContext context = new VelocityContext();
			context.put("amount", info.getAmount());
			context.put("payingUser", payingUser.getUsername());

			UUID picnicId = activities.getAllActivities().stream()
				.filter(activity -> activity.getTitle().contains("elcome"))
				.findAny().get().getId();

			String picniclink = new StringBuilder()
				.append("our <a href=\"")
				.append(hhupAddress)
				.append(ACTIVITIES_PATH)
				.append(picnicId.toString())
				.append("\">Welcome Picnic</a>")
				.toString();

			context.put("picniclink", picniclink);

			StringBuilder paidFor = new StringBuilder();
			List<UUID> paidForIds = info.getUserInfo().getPaidFor();

			// only paid for theirself or (also) for others?
			if (!((paidForIds.size() == 1) && paidForIds.get(0).equals(payingUserId))) {
				paidFor.append("for the following users: \n <ul>");
				for (UUID id : paidForIds) {
					paidFor.append("<li>")
						.append(users.getUserForId(id).getUsername())
						.append("<li>");
				}
				paidFor.append("</ul>");
			}

			context.put("paidFor", paidFor.toString());

			sendMail("paypal payment received", "/templates/paypal.vt", "", context, payingUser.getEmail());

		} catch (UserNotFoundException | MailException | ResourceNotFoundException | ParseErrorException | MessagingException e) {
			log.error("error while sending \"paypal payment received\" mail", e);
		}
	}
}