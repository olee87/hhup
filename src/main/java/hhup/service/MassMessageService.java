package hhup.service;

import hhup.model.exception.ActivityNotFoundException;
import hhup.model.exception.UserNotFoundException;
import hhup.model.history.HistoryType;
import hhup.model.user.InternalUserInfo;

import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Service
public class MassMessageService {

	private String loginMessage = StringUtils.EMPTY;
	private boolean loginMessageActive = false;

	@Autowired
	private ActivitiesService activities;

	@Autowired
	private UserService users;

	@Autowired
	private MailService mailer;

	@Autowired
	private HistoryService history;

	private Logger log = LoggerFactory.getLogger(MassMessageService.class);

	public void setLoginMessage(String loginMessage, boolean active) throws UserNotFoundException {
		this.loginMessageActive = active;
		this.loginMessage = loginMessage;

		InternalUserInfo requestingUser = users.getCurrentUser();
		String status = active ? "active" : "inactive";
		String message = requestingUser.getUsername() + " set the login message to '" + loginMessage + "' (" + status + ")";
		log.info(message);
		history.recordEvent(requestingUser.getId(), HistoryType.SENT_MASS_MAIL, message);
	}

	public String getLoginMessage() {
		return loginMessage;
	}

	public boolean isLoginMessageActive() {
		return loginMessageActive;
	}

	/**
	 *
	 * @param activityId id of the activity to the users of which to send the mass mail to. May be null to send to ALL participants!
	 * @throws ActivityNotFoundException
	 * @throws UserNotFoundException
	 * @throws MessagingException
	 */
	public void sendMassMail(UUID activityId, String text, String subject) throws ActivityNotFoundException,
			UserNotFoundException, MessagingException {

		Set<String> addresses;
		if (activityId != null) {
			addresses = usersToEmails(idsToUsers(activities.getActivity(activityId).getParticipants()));
		} else {
			addresses = usersToEmails(users.getAll());
		}

		if (addresses.isEmpty()) {
			log.debug("not sending mass mail, no addresses defined.");
			return;
		}

		mailer.sendGenericMail(text, subject, Iterables.toArray(addresses, String.class));

		InternalUserInfo requestingUser = users.getCurrentUser();
		String message = requestingUser.getUsername() + " sent a mass mail to "
				+ addresses.size() + " addresses: '" + text + "'";

		log.info(message + " addresses: [" + StringUtils.join(addresses, ", ") + "]");
		history.recordEvent(requestingUser.getId(), HistoryType.SENT_MASS_MAIL, message);
	}

	private Set<String> usersToEmails(Set<InternalUserInfo> users) {
		return Sets.newHashSet(Iterables.transform(users, user -> user.getEmail()));
	}

	private Set<InternalUserInfo> idsToUsers(Set<UUID> ids) {
		Set<InternalUserInfo> result = Sets.newHashSet();
		for (UUID id : ids) {
			try {
				result.add(users.getUserForId(id));
			} catch (UserNotFoundException e) { /* do nothing */ }
		}
		return result;
	}
}