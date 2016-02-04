package hhup.web.controller;

import hhup.model.exception.UserNotFoundException;
import hhup.model.token.RememberMeToken;
import hhup.service.RememberMeService;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest")
public class LoginController {

	private final Logger log = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private RememberMeService rememberMe;

	/**
	 * GET /rest/authenticate -> check if the user is authenticated, and return its login.
	 */
	@RequestMapping(value = "/authenticate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String isAuthenticated(HttpServletRequest request) {
		log.debug("REST request to check if the current user is authenticated");
		return request.getRemoteUser();
	}

	/**
	 * GET /rest/account/sessions -> get the current open sessions.
	 */
	@RequestMapping(value = "/account/sessions", method = RequestMethod.GET)
	@ResponseBody
	public List<RememberMeToken> getCurrentSessions(HttpServletResponse response) {
		try {
			return rememberMe.getRememberMeTokensForCurrentUser();
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	/**
	 * DELETE /rest/account/sessions?series={series} -> invalidate an existing session.
	 *
	 * - You can only delete your own sessions, not any other user's session - If you delete one of your existing
	 * sessions, and that you are currently logged in on that session, you will still be able to use that session, until
	 * you quit your browser: it does not work in real time (there is no API for that), it only removes the
	 * "remember me" cookie - This is also true if you invalidate your current session: you will still be able to use it
	 * until you close your browser or that the session times out. But automatic login (the "remember me" cookie) will
	 * not work anymore. There is an API to invalidate the current session, but there is no API to check which session
	 * uses which cookie.
	 *
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/account/sessions/{sessionId}", method = RequestMethod.DELETE)
	public void invalidateSession(@PathVariable String series, HttpServletResponse response)
			throws UnsupportedEncodingException {
		String decodedSeries = URLDecoder.decode(series, "UTF-8");
		List<RememberMeToken> persistentTokens;
		try {
			persistentTokens = rememberMe.getRememberMeTokensForCurrentUser();
			for (RememberMeToken persistentToken : persistentTokens) {
				if (StringUtils.equals(persistentToken.getSeries(), decodedSeries)) {
					rememberMe.removeRememberMeToken(persistentToken);
				}
			}
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}
}