package hhup.web.controller;

import hhup.model.activity.Activity;
import hhup.model.exception.ActivityNotFoundException;
import hhup.model.exception.UserNotFoundException;
import hhup.service.ActivitiesService;
import hhup.service.AdminService;
import hhup.service.MassMessageService;
import hhup.service.UserService;
import hhup.web.request.CheckinRequest;
import hhup.web.request.ConfirmPaymentRequest;
import hhup.web.request.LoginMessageRequest;
import hhup.web.request.MakeAdminRequest;
import hhup.web.request.MassMailRequest;
import hhup.web.request.ReReadInRequest;
import hhup.web.request.SendTestMailRequest;
import hhup.web.request.SetActiveRequest;
import hhup.web.request.UUIDRequest;

import java.io.IOException;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AdminController {

	@Autowired
	private UserService users;

	@Autowired
	private ActivitiesService activities;

	@Autowired
	private AdminService admin;

	@Autowired
	private MassMessageService massMessages;

	@Secured("ADMIN")
	@RequestMapping(value = "/admin/makeAdmin", method = RequestMethod.POST)
	@ResponseBody
	public void makeAdmin(@RequestBody MakeAdminRequest request, HttpServletResponse response) {
		try {
			users.makeAdmin(request);
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/admin/deleteUser", method = RequestMethod.POST)
	@ResponseBody
	public void deleteUser(@RequestBody UUIDRequest request, HttpServletResponse response) {
		try {
			users.deleteUser(request.getId());
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/admin/setActive", method = RequestMethod.POST)
	@ResponseBody
	public void setActive(@RequestBody SetActiveRequest request, HttpServletResponse response) {
		try {
			users.setActive(request);
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/admin/checkin", method = RequestMethod.POST)
	@ResponseBody
	public void checkin(@RequestBody CheckinRequest request, HttpServletResponse response) {
		try {
			users.checkIn(request);
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/admin/confirmPayment", method = RequestMethod.POST)
	@ResponseBody
	public void setActive(@RequestBody ConfirmPaymentRequest request, HttpServletResponse response) {
		try {
			users.confirmPayment(request);
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/admin/createActivity", method = RequestMethod.POST)
	@ResponseBody
	public void createActivity(@RequestBody Activity activity) throws UserNotFoundException {
		activities.createActivity(activity);
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/admin/removeActivity/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void removeActivity(@PathVariable UUID id, HttpServletResponse response) throws UserNotFoundException {
		try {
			activities.removeActivity(id);
		} catch (ActivityNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/admin/editActivity", method = RequestMethod.POST)
	@ResponseBody
	public void editActivity(@RequestBody Activity activity, HttpServletResponse response)
			throws UserNotFoundException {
		try {
			activities.editActivity(activity);
		} catch (ActivityNotFoundException e) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/admin/reReadIn", method = RequestMethod.POST)
	@ResponseBody
	public void reReadIn(@RequestBody ReReadInRequest request) throws UserNotFoundException {
		admin.reReadIn(request.getType());
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/rest/admin/massMail", method = RequestMethod.POST)
	@ResponseBody
	public void setEmergencyMessage(@RequestBody MassMailRequest request) throws UserNotFoundException,
			ActivityNotFoundException, MessagingException {
		massMessages.sendMassMail(request.getActivityId(), request.getText(), request.getSubject());
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/rest/admin/loginMessage", method = RequestMethod.POST)
	@ResponseBody
	public void setLoginMessage(@RequestBody LoginMessageRequest request) throws UserNotFoundException {
		massMessages.setLoginMessage(request.getMessage(), request.isActive());
	}

	@Secured("ADMIN")
	@RequestMapping(value = "/rest/admin/testMail", method = RequestMethod.POST)
	@ResponseBody
	public void testMail(@RequestBody SendTestMailRequest request, HttpServletResponse response) throws IOException,
			UserNotFoundException {
		try {
			admin.testMail(request.getAddress());
		} catch (MessagingException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.getOutputStream().write(e.getMessage().getBytes());
		}
	}
}