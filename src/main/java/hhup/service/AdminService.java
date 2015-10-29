package hhup.service;

import hhup.model.activity.Activity;
import hhup.model.couchmarket.CouchMarketPost;
import hhup.model.exception.UserNotFoundException;
import hhup.model.guestbook.GuestbookEntry;
import hhup.model.history.HistoryType;
import hhup.model.token.AccountActivationToken;
import hhup.model.token.PasswordRecoveryToken;
import hhup.model.token.RememberMeToken;
import hhup.model.user.InternalUserInfo;
import hhup.repository.Repository;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

	@Autowired
	@Qualifier("AccountActivationTokenRepository")
	private Repository<AccountActivationToken> accountActivation;

	@Autowired
	@Qualifier("ActivityRepository")
	private Repository<Activity> activities;

	@Autowired
	@Qualifier("CouchMarketPostRepository")
	private Repository<CouchMarketPost> couchMarket;

	@Autowired
	@Qualifier("GuestbookRepository")
	private Repository<GuestbookEntry> guestbook;

	@Autowired
	@Qualifier("PasswordRecoveryTokenRepository")
	private Repository<PasswordRecoveryToken> passwordToken;

	@Autowired
	@Qualifier("RememberMeTokenRepository")
	private Repository<RememberMeToken> rememberMe;

	@Autowired
	@Qualifier("UserRepository")
	private Repository<InternalUserInfo> users;

	@Autowired
	private HistoryService history;

	@Autowired
	private UserService userService;

	@Autowired
	private MailService mailer;

	public void testMail(String address) throws MessagingException, UserNotFoundException {
		mailer.sendTestMail(address);


		InternalUserInfo user = userService.getCurrentUser();
		String message = testMailMessage(user, address);
		history.recordEvent(user.getId(), HistoryType.SENT_TEST_MAIL, message);
	}

	public void reReadIn(String type) throws UserNotFoundException {
		switch (StringUtils.strip(type)) {
			case "all": {
				accountActivation.reload();
				activities.reload();
				couchMarket.reload();
				guestbook.reload();
				passwordToken.reload();
				rememberMe.reload();
				users.reload();
				break;
			}
			case "users": {
				users.reload();
				break;
			}
			case "activities": {
				activities.reload();
				break;
			}
			case "guestbook": {
				guestbook.reload();
				break;
			}
			case "couchmarket": {
				couchMarket.reload();
				break;
			}
		}

		InternalUserInfo user = userService.getCurrentUser();
		String message = reReadInMessage(user, type);
		history.recordEvent(user.getId(), HistoryType.RE_READ_IN, message);
	}

	private String reReadInMessage(InternalUserInfo user, String type) {
		return user.getUsername() + " re-read repository from json: " + type;
	}

	private String testMailMessage(InternalUserInfo user, String mail) {
		return user.getUsername() + " sent a test mail to '" + mail + "'";
	}
}