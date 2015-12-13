package hhup.service;

import hhup.model.exception.NoSuchTokenException;
import hhup.model.exception.UnauthorizedException;
import hhup.model.exception.UserNotFoundException;
import hhup.model.history.HistoryType;
import hhup.model.token.AccountActivationToken;
import hhup.model.token.PasswordRecoveryToken;
import hhup.model.user.Authority;
import hhup.model.user.InternalUserInfo;
import hhup.model.user.NewUserInfo;
import hhup.model.user.UserBuilder;
import hhup.repository.Repository;
import hhup.web.request.CheckinRequest;
import hhup.web.request.ConfirmPaymentRequest;
import hhup.web.request.EditUserRequest;
import hhup.web.request.EmailOrUsernameRequest;
import hhup.web.request.MakeAdminRequest;
import hhup.web.request.PaypalPaymentInfo;
import hhup.web.request.RecoverPasswordRequest;
import hhup.web.request.SetActiveRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Service class for managing users.
 */
@Service
public class UserService implements UserDetailsService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";

	@Autowired
	@Qualifier("UserRepository")
	private Repository<InternalUserInfo> users;

	@Autowired
	@Qualifier("AccountActivationTokenRepository")
	private Repository<AccountActivationToken> accountActivationTokens;

	@Autowired
	@Qualifier("PasswordRecoveryTokenRepository")
	private Repository<PasswordRecoveryToken> passwordRecoveryTokens;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ActivitiesService activities;

	@Autowired
	private MailService mailer;

	@Autowired
	private HistoryService history;

	public void createUser(NewUserInfo newUserInfo) {
		log.debug("registering: {}.", newUserInfo.getUsername());

		UserBuilder newUser = UserBuilder.from(newUserInfo);

		log.trace("encoding password");
		String unencodedPassword = newUserInfo.getPassword();
		String encodedPassword = passwordEncoder.encode(unencodedPassword);
		newUser.withPassword(encodedPassword);

		log.trace("saving registration date/time");
		newUser.withRegistrationDate(DateTime.now());

		log.trace("generating user id");
		UUID id = UUID.nameUUIDFromBytes(newUserInfo.getUsername().getBytes());
		newUser.withId(id);

		log.trace("setting default authority \"USER\"");
		newUser.withAdditionalAuthority(Authority.USER);

		log.trace("finalizing internal user info");
		InternalUserInfo user = newUser.asInternalUserInfo();

		log.trace("validating user");
		validate(user);

		try {
			log.debug("registration for {} valid, sending confirmation request email.", user);
			UUID tokenCode = UUID.randomUUID();
			users.add(user);
			accountActivationTokens.add(new AccountActivationToken(id, tokenCode));
			log.trace("user saved");
			mailer.sendRegistrationConfirmation(tokenCode, user);
			history.recordEvent(id, HistoryType.SIGNED_UP, signupMessage(user), id);
			log.trace("mail sent");
			log.info("successfully registered user {}.", user);
		} catch (MessagingException e) {
			log.warn("could not send confirmation mail to user {} ({}), reason: {}", user.getUsername(),
					user.getEmail(), e);
		}
	}

	public InternalUserInfo activateRegistration(UUID vaule) throws NoSuchTokenException, UserNotFoundException {
		log.debug("Activating user for activation key {}", vaule);

		AccountActivationToken token = accountActivationTokens.findFirst(x -> vaule.equals(x.getValue()));

		if (token == null) { throw new NoSuchTokenException(vaule); }

		InternalUserInfo user = users.findFirst(x -> x.getId().equals(token.getUserId()));

		if (user == null) { throw new UserNotFoundException(token.getUserId()); }

		InternalUserInfo activatedUser = UserBuilder.from(user).withActivated(true).asInternalUserInfo();

		users.replace(activatedUser);

		accountActivationTokens.removeFirst(x -> vaule.equals(x.getValue()));

		String message = activatedSelfMessage(activatedUser);
		history.recordEvent(token.getUserId(), HistoryType.ACTIVATED, message, token.getUserId());
		return activatedUser;
	}

	public void editUserInformation(EditUserRequest edit) throws UnauthorizedException, UserNotFoundException {
		InternalUserInfo requestingUser = getCurrentUser();

		UserBuilder builder;
		if ((edit.getUserId() != null) && !edit.getUserId().equals(requestingUser.getId())) {
			// another user is supposed to be edited. only admins may do that.
			if (isAdmin(requestingUser)) {
				InternalUserInfo editedUser = getUserForId(edit.getUserId());
				builder = UserBuilder.from(editedUser);
			} else {
				throw new UnauthorizedException();
			}
		} else {
			builder = UserBuilder.from(requestingUser);
		}

		if (edit.getBwProfile() != null) {
			builder.withBwProfile(edit.getBwProfile());
		}
		if (edit.getFbProfile() != null) {
			builder.withFbProfile(edit.getFbProfile());
		}
		if (edit.getHcProfile() != null) {
			builder.withHcProfile(edit.getHcProfile());
		}
		if (edit.getCsProfile() != null) {
			builder.withCsProfile(edit.getCsProfile());
		}
		if (edit.getNationality() != null) {
			builder.withNationality(edit.getNationality());
		}
		if (edit.getHomeId() != null) {
			builder.withHomeId(edit.getHomeId());
			builder.withHomeString(null);
		} else if (edit.getHomeString() != null) {
			builder.withHomeId(null);
			builder.withHomeString(edit.getHomeString());
		}

		if (edit.getPhone() != null) {
			builder.withPhone(edit.getPhone());
		}

		if (edit.getLanguages() != null) {
			builder.withLanguages(edit.getLanguages());
		}

		if (edit.getRealName() != null) {
			builder.withFirstName(edit.getRealName());
		}

		InternalUserInfo editedUser = builder.asInternalUserInfo();
		users.replace(editedUser);

		String message = editProfileMessage(requestingUser, editedUser);
		history.recordEvent(requestingUser.getId(), HistoryType.EDITED_PROFILE, message, edit.getUserId());
	}

	public void changePassword(String password) throws UserNotFoundException {
		InternalUserInfo currentUser = getCurrentUser();
		String encryptedPassword = passwordEncoder.encode(password);

		InternalUserInfo updatedUserInfo = UserBuilder.from(currentUser).withPassword(encryptedPassword)
				.asInternalUserInfo();

		users.replace(updatedUserInfo);

		String message = changeOwnPasswordMessage(currentUser);
		history.recordEvent(currentUser.getId(), HistoryType.CHANGED_PASSWORD, message, currentUser.getId());
		log.debug("Changed password for User: {}", currentUser);
	}

	public void makeAdmin(MakeAdminRequest request) throws UserNotFoundException {
		InternalUserInfo user = getUserForId(request.getUserId());
		InternalUserInfo requestingUser = getCurrentUser();

		UserBuilder builder = UserBuilder.from(user);
		if (request.isMakeAdmin()) {
			builder.withAdditionalAuthority(Authority.ADMIN);
		} else {
			builder.withoutAuthority(Authority.ADMIN);
		}
		InternalUserInfo updatedUserInfo = builder.asInternalUserInfo();

		users.replace(updatedUserInfo);

		HistoryType type = request.isMakeAdmin() ? HistoryType.MADE_ADMIN : HistoryType.UNMADE_ADMIN;
		String message = adminMessage(requestingUser, user, request.isMakeAdmin());
		history.recordEvent(requestingUser.getId(), type, message, user.getId());
		log.info("User: {} is {} ADMIN.", user.getUsername(), request.isMakeAdmin() ? "now" : "no longer");
	}

	public void confirmPayment(ConfirmPaymentRequest request) throws UserNotFoundException {
		InternalUserInfo user = getUserForId(request.getUserId());
		InternalUserInfo requestingUser = getCurrentUser();

		if (request.isPaid() == null) {
			log.warn("no payment info in confirmation request!!!");
			return;
		}

		InternalUserInfo updatedUserInfo = UserBuilder.from(user).withPaid(request.isPaid())
				.withPayDate(DateTime.now()).asInternalUserInfo();

		users.replace(updatedUserInfo);

		HistoryType type = request.isPaid() ? HistoryType.MARKED_PAID : HistoryType.UNMARKED_PAID;
		String message = paidMessage(requestingUser, user, request.isPaid());
		history.recordEvent(requestingUser.getId(), type, message, user.getId());
		log.info("User: {} is {} confirmed as paid.", user.getUsername(), request.isPaid() ? "now" : "no longer");
	}

	public void setActive(SetActiveRequest request) throws UserNotFoundException {
		InternalUserInfo user = getUserForId(request.getUserId());
		InternalUserInfo requestingUser = getCurrentUser();

		if (request.isActive() == null) {
			log.warn("no activation info in activation request!!!");
			return;
		}

		InternalUserInfo updatedUserInfo = UserBuilder.from(user).withActivated(request.isActive())
				.asInternalUserInfo();

		users.replace(updatedUserInfo);

		HistoryType type = request.isActive() ? HistoryType.ACTIVATED : HistoryType.DEACTIVATED;
		String message = activationMessage(requestingUser, user, request.isActive());
		history.recordEvent(requestingUser.getId(), type, message, user.getId());
		log.info("User: {} is {} activated.", user.getUsername(), request.isActive() ? "now" : "no longer");
	}

	public void changePassword(UUID userId, String password) throws UserNotFoundException {
		InternalUserInfo user = getUserForId(userId);
		InternalUserInfo requestingUser = getCurrentUser();

		String encryptedPassword = passwordEncoder.encode(password);

		InternalUserInfo updatedUserInfo = UserBuilder.from(user).withPassword(encryptedPassword).asInternalUserInfo();

		users.replace(updatedUserInfo);

		String message = changeOthersPasswordMessage(requestingUser, user);
		history.recordEvent(requestingUser.getId(), HistoryType.CHANGED_PASSWORD, message, user.getId());
		log.debug("Changed password for User: {}", user);
	}

	public InternalUserInfo getUserForUsername(String username) throws UserNotFoundException {
		InternalUserInfo user = users.findFirst(x -> username.equalsIgnoreCase(x.getUsername()));
		if (user == null) { throw new UserNotFoundException(username); }
		return user;
	}

	public InternalUserInfo getUserForId(UUID userId) throws UserNotFoundException {
		InternalUserInfo user = users.findFirst(x -> x.getId().equals(userId));
		if (user == null) { throw new UserNotFoundException(userId); }
		return user;
	}

	public InternalUserInfo getUserForEmailOrUsername(String emailOrUsername) throws UserNotFoundException {
		if (emailOrUsername.matches(EMAIL_PATTERN)) { return getUserForEmail(emailOrUsername); }
		return getUserForUsername(emailOrUsername);
	}

	public InternalUserInfo getCurrentUser() throws UserNotFoundException {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		UserDetails springSecurityUser = null;
		String userName = null;

		if (authentication != null) {
			if (authentication.getPrincipal() instanceof UserDetails) {
				springSecurityUser = (UserDetails) authentication.getPrincipal();
				userName = springSecurityUser.getUsername();
			} else if (authentication.getPrincipal() instanceof String) {
				userName = (String) authentication.getPrincipal();
			}
		}
		return getUserForEmailOrUsername(userName);
	}

	@Scheduled(cron = "0 0 1 * * ?")
	public void removeNotActivatedUsers() {
		log.debug("Deleting not activated users");
		DateTime minRegisterDate = DateTime.now().minusDays(3);
		users.removeAll(x -> x.getRegistrationDate().isBefore(minRegisterDate) && !x.isActivated());
	}

	public boolean isUsernameTaken(String username) {
		try {
			getUserForUsername(username);
		} catch (UserNotFoundException e) {
			return false;
		}
		return true;
	}

	public boolean isEmailUsed(String email) {
		return users.containsAny(x -> x.getEmail().equals(email));
	}

	private void validate(InternalUserInfo newUser) {
		// check username
		if (newUser.getUsername() == null) { throw new RuntimeException("username is 'null'"); }
		if (isUsernameTaken(newUser.getUsername())) { throw new RuntimeException("username is already taken"); }

		// check password
		if (newUser.getPassword() == null) { throw new RuntimeException("password is 'null'"); }

		// check email
		if (newUser.getEmail() == null) { throw new RuntimeException("email is 'null'"); }
		if (isEmailUsed(newUser.getEmail())) { throw new RuntimeException("email is already in use"); }
	}

	public boolean isAdmin(UUID userId) throws UserNotFoundException {
		return isAdmin(getUserForId(userId));
	}

	public boolean isAdmin(InternalUserInfo user) {
		return user.getAuthorities().contains(Authority.ADMIN);
	}

	public Set<InternalUserInfo> getAll() {
		return ImmutableSet.copyOf(users);
	}

	public void requestPasswordRecovery(EmailOrUsernameRequest request) throws UserNotFoundException, MessagingException {
		InternalUserInfo user = null;
		if (request.getEmail() != null) {
			user = getUserForEmail(request.getEmail());
		} else if (request.getUsername() != null) {
			user = getUserForUsername(request.getUsername());
		}

		if (user != null) {
			final UUID userId = user.getId();
			passwordRecoveryTokens.removeAll(x -> x.getUserId().equals(userId));
			UUID token = UUID.randomUUID();
			mailer.sendPasswordRecoveryMail(user, token);
			passwordRecoveryTokens.add(new PasswordRecoveryToken(user, token));

			String message = pwRecoveryRequestMessage(user);
			history.recordEvent(user.getId(), HistoryType.REQUESTED_PASSWORD_RECOVERY, message, user.getId());
		}
	}

	public InternalUserInfo getUserForEmail(String email) throws UserNotFoundException {
		InternalUserInfo user = users.findFirst(x -> x.getEmail().equalsIgnoreCase(email));
		if (user == null) { throw new UserNotFoundException(email); }
		return user;
	}

	public void comfirmPasswordRecovery(RecoverPasswordRequest request) throws NoSuchTokenException,
			UserNotFoundException {
		PasswordRecoveryToken token = passwordRecoveryTokens.findFirst(x -> x.getToken().equals(request.getToken()));
		if (token == null) { throw new NoSuchTokenException(request.getToken()); }
		String newPassword = passwordEncoder.encode(request.getPassword());
		InternalUserInfo user = getUserForId(token.getUserId());
		InternalUserInfo updatedUser = UserBuilder.from(user).withPassword(newPassword).asInternalUserInfo();
		users.replace(updatedUser);
		passwordRecoveryTokens.removeAll(x -> x.getUserId().equals(token.getUserId()));

		String message = pwRecoveryConfirmedMessage(user);
		history.recordEvent(user.getId(), HistoryType.CONFIRMED_PASSWORD_RECOVERY, message, user.getId());
	}

	public void checkIn(CheckinRequest request) throws UserNotFoundException {
		InternalUserInfo user = getUserForId(request.getUserId());
		InternalUserInfo requestingUser = getCurrentUser();

		if (request.isCheckin() == null) {
			log.warn("missing info in checkin request!!!");
			return;
		}

		InternalUserInfo updatedUserInfo = UserBuilder.from(user).withCheckedIn(request.isCheckin())
				.withCheckinDate(request.isCheckin() ? DateTime.now() : null).asInternalUserInfo();

		users.replace(updatedUserInfo);

		log.info("User: {} is {} checked in.", user.getUsername(), request.isCheckin() ? "now" : "no longer");

		HistoryType type = request.isCheckin() ? HistoryType.MARKED_CHECKED_IN : HistoryType.UNMARKED_CHECKED_IN;
		String message = checkinMessage(requestingUser, user, request.isCheckin());
		history.recordEvent(requestingUser.getId(), type, message, user.getId());
	}

	public int getCountryCount() {
		return Sets.newHashSet(Lists.transform(Lists.newArrayList(users), x -> x.getNationality())).size();
	}

	private String checkinMessage(InternalUserInfo requestingUser, InternalUserInfo user, Boolean checkin) {
		String not = checkin ? "" : " not";
		String subject = user.equals(requestingUser) ? "theirself" : user.getUsername();
		return requestingUser.getUsername() + " marked " + subject + " as" + not + " checked in";
	}

	private String pwRecoveryConfirmedMessage(InternalUserInfo user) {
		return user.getUsername() + " confirmed their password recovery request";
	}

	private String pwRecoveryRequestMessage(InternalUserInfo user) {
		return user.getUsername() + " requested a password recovery";
	}

	private String signupMessage(InternalUserInfo user) {
		return user.getUsername() + " signed up for HHUP";
	}

	private String editProfileMessage(InternalUserInfo requestingUser, InternalUserInfo user) {
		String other = requestingUser.equals(user) ? "their" : user.getUsername() + "'s";
		return requestingUser.getUsername() + " edited " + other + " profile";
	}

	private String changeOthersPasswordMessage(InternalUserInfo requestingUser, InternalUserInfo user) {
		return requestingUser.getUsername() + " changed " + user.getUsername() + "'s password";
	}

	private String changeOwnPasswordMessage(InternalUserInfo user) {
		return user.getUsername() + " changed their password";
	}

	private String activatedSelfMessage(InternalUserInfo user) {
		return user.getUsername() + " activated their account";
	}

	private String activationMessage(InternalUserInfo requestingUser, InternalUserInfo user, Boolean active) {
		String de = active ? "" : "de";
		String subject = user.equals(requestingUser) ? "their own" : user.getUsername() + "'s";
		return requestingUser.getUsername() + " " + de + "activated " + subject + " account";
	}

	private String paidMessage(InternalUserInfo requestingUser, InternalUserInfo user, Boolean paid) {
		String not = paid ? "" : "not ";
		String subject = user.equals(requestingUser) ? "their own" : user.getUsername() + "'s";
		return requestingUser.getUsername() + " set " + subject + " payment status to " + not + "paid";
	}

	private String adminMessage(InternalUserInfo requestingUser, InternalUserInfo user, Boolean admin) {
		String un = admin ? "" : "un";
		String subject = user.equals(requestingUser) ? "theirself" : user.getUsername();
		return requestingUser.getUsername() + " " + un + "made " + subject + " admin";
	}

	public void setPaypalPaid(PaypalPaymentInfo info) throws UserNotFoundException {

		InternalUserInfo payingUser = getUserForId(info.getUserInfo().getPayingUser());
		for (UUID userId : info.getUserInfo().getPaidFor()) {
			InternalUserInfo user = getUserForId(userId);
			InternalUserInfo updatedUserInfo = UserBuilder.from(user).withPaid(true)
					.withPayDate(info.getDate()).asInternalUserInfo();

			users.replace(updatedUserInfo);

			String message = paypalPaidMessage(payingUser, user);
			history.recordEvent(userId, HistoryType.PAYPAL_PAID, message);
			log.info(message);
		}
	}

	private String paypalPaidMessage(InternalUserInfo payingUser, InternalUserInfo user) {
		String otherUser = payingUser.equals(user) ? "" : "for " + user.getUsername() + " ";
		return payingUser.getUsername() + " paid " + otherUser + "through paypal.";
	}

	@Override
	public UserDetails loadUserByUsername(final String login) {
		log.debug("Authenticating {}", login);
		String lowercaseLogin = login.toLowerCase();

		InternalUserInfo user;
		try {
			user = getUserForEmailOrUsername(lowercaseLogin);
			if (user == null) {
				throw new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database");
			} else if ((user.isActivated() == null) || !user.isActivated()) { throw new RuntimeException("User "
					+ lowercaseLogin + " was not activated"); }
		} catch (UserNotFoundException e) {
			throw new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database");
		}

		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		for (Authority authority : user.getAuthorities()) {
			GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority.name());
			grantedAuthorities.add(grantedAuthority);
		}

		String message = user.getUsername() + " logged in";
		history.recordEvent(user.getId(), HistoryType.LOGGED_IN, message);

		return new org.springframework.security.core.userdetails.User(lowercaseLogin, user.getPassword(),
				grantedAuthorities);
	}

	public void deleteUser(UUID id) throws UserNotFoundException {
		InternalUserInfo user = getUserForId(id);
		log.error("would delete user: " + user);
		// TODO implement integrity checks
	}
}