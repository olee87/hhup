package hhup.service;

import hhup.HHUPConstants;
import hhup.model.exception.NoSuchTokenException;
import hhup.model.exception.UserNotFoundException;
import hhup.model.history.HistoryType;
import hhup.model.token.RememberMeToken;
import hhup.model.user.InternalUserInfo;
import hhup.repository.Repository;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

/**
 * Custom implementation of Spring Security's RememberMeServices.
 * <p/>
 * Persistent tokens are used by Spring Security to automatically log in users.
 * <p/>
 * This is a specific implementation of Spring Security's remember-me authentication, but it is much more powerful than
 * the standard implementations:
 * <ul>
 * <li>It allows a user to see the list of his currently opened sessions, and invalidate them</li>
 * <li>It stores more information, such as the IP address and the user agent, for audit purposes
 * <li>
 * <li>When a user logs out, only his current session is invalidated, and not all of his sessions</li>
 * </ul>
 * <p/>
 * This is inspired by:
 * <ul>
 * <li><a href="http://jaspan.com/improved_persistent_login_cookie_best_practice" >Improved Persistent Login Cookie Best
 * Practice</a></li>
 * <li><a href="https://github.com/blog/1661-modeling-your-app-s-user-session">Github's
 * "Modeling your App's User Session"</a></li></li>
 * </ul>
 * <p/>
 * The main algorithm comes from Spring Security's PersistentTokenBasedRememberMeServices, but this class couldn't be
 * cleanly extended.
 * <p/>
 */
@Service
public class RememberMeService extends AbstractRememberMeServices {

	private final Logger log = LoggerFactory.getLogger(RememberMeService.class);

	// Token is valid for one month
	private static final int TOKEN_VALIDITY_DAYS = 31;

	private static final int TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * TOKEN_VALIDITY_DAYS;

	private static final int DEFAULT_SERIES_LENGTH = 16;

	private static final int DEFAULT_TOKEN_LENGTH = 16;

	private SecureRandom random;

	@Autowired
	private UserService users;

	@Autowired
	private HistoryService history;

	@Autowired
	@Qualifier("RememberMeTokenRepository")
	private Repository<RememberMeToken> tokens;

	@Autowired
	public RememberMeService(UserService userDetailsService) {

		super(HHUPConstants.REMEMBER_ME_KEY, userDetailsService);
		random = new SecureRandom();
	}

	@Override
	protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request,
			HttpServletResponse response) {

		RememberMeToken token = getPersistentToken(cookieTokens);
		String login;
		try {
			login = users.getUserForId(token.getUserId()).getUsername();
		} catch (UserNotFoundException e1) {
			throw new RememberMeAuthenticationException("user in token not found");
		}

		// Token also matches, so login is valid. Update the token value,
		// keeping the *same* series number.
		log.debug("Refreshing persistent login token for user '{}', series '{}'", login, token.getSeries());
		token.setTokenDate(new LocalDate());
		token.setTokenValue(generateTokenData());
		token.setIpAddress(request.getRemoteAddr());
		token.setUserAgent(request.getHeader("User-Agent"));
		save(token);
		addCookie(token, request, response);
		return getUserDetailsService().loadUserByUsername(login);
	}

	@Override
	protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication successfulAuthentication) {
		String login = successfulAuthentication.getName();

		log.info("Creating new persistent login for user {}", login);
		InternalUserInfo user;
		try {
			user = users.getUserForEmailOrUsername(login);
			RememberMeToken token = new RememberMeToken();
			token.setSeries(generateSeriesData());
			token.setUserId(user.getId());
			token.setTokenValue(generateTokenData());
			token.setTokenDate(new LocalDate());
			token.setIpAddress(request.getRemoteAddr());
			token.setUserAgent(request.getHeader("User-Agent"));
			save(token);
			addCookie(token, request, response);
		} catch (UserNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * When logout occurs, only invalidate the current token, and not all user sessions.
	 * <p/>
	 * The standard Spring Security implementations are too basic: they invalidate all tokens for the current user, so
	 * when he logs out from one browser, all his other sessions are destroyed.
	 */
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		String rememberMeCookie = extractRememberMeCookie(request);
		if ((rememberMeCookie != null) && (rememberMeCookie.length() != 0)) {
			try {
				String[] cookieTokens = decodeCookie(rememberMeCookie);
				RememberMeToken token = getPersistentToken(cookieTokens);

				InternalUserInfo user = users.getUserForId(token.getUserId());
				String message = user.getUsername() + " logged out";
				history.recordEvent(token.getUserId(), HistoryType.LOGGED_OUT, message);
				delete(token);
			} catch (InvalidCookieException ice) {
				log.info("Invalid cookie, no persistent token could be deleted");
			} catch (RememberMeAuthenticationException rmae) {
				log.debug("No persistent token found, so no token could be deleted");
			} catch (UserNotFoundException e) {
				e.printStackTrace();
			}
		}
		super.logout(request, response, authentication);
	}

	public List<RememberMeToken> getRememberMeTokensForUser(final InternalUserInfo user) {
		return ImmutableList.copyOf(tokens.findAll(x -> x.getUserId().equals(user.getId())));
	}

	public List<RememberMeToken> getRememberMeTokensForCurrentUser() throws UserNotFoundException {
		return getRememberMeTokensForUser(users.getCurrentUser());
	}

	public void removeRememberMeToken(RememberMeToken token) {
		tokens.remove(token);
	}

	@Scheduled(cron = "0 0 0 * * ?")
	public void removeOldPersistentTokens() {
		log.debug("Deleting unused tokens");
		tokens.retainAll(x -> x.getTokenDate().isAfter(new LocalDate().minusMonths(1)));
	}

	/**
	 * Validate the token and return it.
	 */
	private RememberMeToken getPersistentToken(String[] cookieTokens) {
		if (cookieTokens.length != 2) { throw new InvalidCookieException("Cookie token did not contain " + 2
				+ " tokens, but contained '" + Arrays.asList(cookieTokens) + "'"); }

		final String presentedSeries = cookieTokens[0];
		final String presentedToken = cookieTokens[1];

		RememberMeToken token;
		try {
			token = findToken(presentedSeries);
		} catch (NoSuchTokenException e) {
			throw new RememberMeAuthenticationException("No persistent token found for series id: " + presentedSeries);
		}

		// We have a match for this user/series combination
		log.info("presentedToken={} / tokenValue={}", presentedToken, token.getTokenValue());
		if (!presentedToken.equals(token.getTokenValue())) {
			// Token doesn't match series value. Delete this session and throw
			// an exception.
			delete(token);
			throw new CookieTheftException(
					"Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack.");
		}

		if (token.getTokenDate().plusDays(TOKEN_VALIDITY_DAYS).isBefore(LocalDate.now())) {
			delete(token);
			throw new RememberMeAuthenticationException("Remember-me login has expired");
		}
		return token;
	}

	private String generateSeriesData() {
		byte[] newSeries = new byte[DEFAULT_SERIES_LENGTH];
		random.nextBytes(newSeries);
		return new String(Base64.encode(newSeries));
	}

	private String generateTokenData() {
		byte[] newToken = new byte[DEFAULT_TOKEN_LENGTH];
		random.nextBytes(newToken);
		return new String(Base64.encode(newToken));
	}

	private void addCookie(RememberMeToken token, HttpServletRequest request, HttpServletResponse response) {
		setCookie(new String[] { token.getSeries(), token.getTokenValue() }, TOKEN_VALIDITY_SECONDS, request, response);
	}

	private RememberMeToken findToken(String presentedSeries) throws NoSuchTokenException {
		RememberMeToken token = tokens.findFirst(x -> x.getSeries().equals(presentedSeries));
		if (token == null) { throw new NoSuchTokenException(presentedSeries); }
		return token;
	}

	private void delete(RememberMeToken token) {
		tokens.remove(token);
	}

	private void save(RememberMeToken token) {
		tokens.add(token);
	}
}