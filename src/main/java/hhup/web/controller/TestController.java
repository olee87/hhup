package hhup.web.controller;

import hhup.model.exception.UserNotFoundException;
import hhup.model.user.InternalUserInfo;
import hhup.service.MailService;
import hhup.service.UserService;

import java.util.UUID;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

	@Autowired
	private UserService users;

	@Autowired
	private MailService mail;

	@RequestMapping(value = "/rest/test/confirmMail", method = RequestMethod.GET)
	@ResponseBody
	public void testConfirmationMail() throws MessagingException, UserNotFoundException {

		InternalUserInfo user = users.getUserForUsername("ole");
		UUID token = UUID.randomUUID();
		mail.sendRegistrationConfirmation(token, user);
	}
}