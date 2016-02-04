package hhup.web.controller;

import hhup.model.exception.NoSuchTokenException;
import hhup.model.exception.UnauthorizedException;
import hhup.model.exception.UserNotFoundException;
import hhup.model.user.InternalUserInfo;
import hhup.model.user.NewUserInfo;
import hhup.model.user.PublicUserInfo;
import hhup.model.user.UserBuilder;
import hhup.model.user.UserUtils;
import hhup.service.MassMessageService;
import hhup.service.UserService;
import hhup.web.request.EditUserRequest;
import hhup.web.request.EmailOrUsernameRequest;
import hhup.web.request.RecoverPasswordRequest;
import hhup.web.response.LoginMessage;

import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest")
public class UserController {

	@Autowired
	private UserService users;

	@Autowired
	private MassMessageService massMessage;

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	@ResponseBody
	public void registerAccount(@RequestBody NewUserInfo user, HttpServletResponse response) {
		if (users.isUsernameTaken(user.getUsername())) {
			response.setStatus(HttpStatus.NOT_MODIFIED.value());
		} else {
			users.createUser(user);
			response.setStatus(HttpStatus.CREATED.value());
		}
	}

	@RequestMapping(value = "/activateUser", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> activateAccount(@RequestParam UUID code) {
		InternalUserInfo user;
		try {
			user = users.activateRegistration(code);
			return new ResponseEntity<>(user.getUsername(), HttpStatus.OK);
		} catch (NoSuchTokenException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (UserNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/user", method = RequestMethod.GET)
	@ResponseBody
	public PublicUserInfo getAccount(HttpServletResponse response) {
		try {
			return UserBuilder.from(users.getCurrentUser()).asPublicUserInfo();
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return null;
		}
	}

	/**
	 * POST /rest/account -> update the current user information.
	 */
	@RequestMapping(value = "/editUser", method = RequestMethod.POST)
	@ResponseBody
	public void saveAccount(@RequestBody EditUserRequest request, HttpServletResponse response) {
		try {
			users.editUserInformation(request);
		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	/**
	 * POST /rest/change_password -> changes the current user's password
	 */
	@RequestMapping(value = "/changePassword", method = RequestMethod.POST)
	@ResponseBody
	public void changePassword(@RequestBody String password, HttpServletResponse response) {
		if (StringUtils.isBlank(password)) {
			response.setStatus(HttpStatus.FORBIDDEN.value());
			return;
		}
		try {
			users.changePassword(password);
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	@ResponseBody
	public Set<PublicUserInfo> getUsers() {
		return UserUtils.toPublicUserInfos(users.getAll());
	}

	@RequestMapping(value = "/users/byUsername/{userName}", method = RequestMethod.GET)
	@ResponseBody
	public PublicUserInfo getProfileByUsername(@PathVariable String userName, HttpServletResponse response) {
		try {
			return UserBuilder.from(users.getUserForUsername(userName)).asPublicUserInfo();
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return null;
		}
	}

	@RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
	@ResponseBody
	public PublicUserInfo getProfileById(@PathVariable UUID id, HttpServletResponse response) {
		try {
			return UserBuilder.from(users.getUserForId(id)).asPublicUserInfo();
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return null;
		}
	}

	@RequestMapping(value = "/recoverPassword", method = RequestMethod.POST)
	@ResponseBody
	public void requestPasswordRecovery(@RequestBody EmailOrUsernameRequest request, HttpServletResponse response)
			throws MessagingException {
		try {
			users.requestPasswordRecovery(request);
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@RequestMapping(value = "/confirmRecoverPassword", method = RequestMethod.POST)
	@ResponseBody
	public void confirmPasswordRecovery(@RequestBody RecoverPasswordRequest request, HttpServletResponse response) {
		try {
			users.comfirmPasswordRecovery(request);
		} catch (NoSuchTokenException | UserNotFoundException e) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
		}
	}

	@RequestMapping(value = "/usernameTaken/{username}", method = RequestMethod.GET)
	@ResponseBody
	public Boolean[] usernameTaken(@PathVariable String username) {
		return new Boolean[] { users.isUsernameTaken(username) };
	}

	@RequestMapping(value = "/loginMessage", method = RequestMethod.GET)
	@ResponseBody
	public LoginMessage getLoginMessage() {
		return new LoginMessage(massMessage.isLoginMessageActive(), massMessage.getLoginMessage());
	}
}