package hhup.web.controller;

import hhup.model.exception.UserNotFoundException;
import hhup.model.guestbook.GuestbookEntry;
import hhup.service.GuestbookService;
import hhup.web.request.CreateGuestbookEntryRequest;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GuestbookController {

	@Autowired
	private GuestbookService guestbook;

	@RequestMapping(value = "/rest/guestbook/all", method = RequestMethod.GET)
	@ResponseBody
	public List<GuestbookEntry> allPosts() {
		return guestbook.getAll();
	}

	@RequestMapping(value = "/rest/guestbook/create", method = RequestMethod.POST)
	@ResponseBody
	public void createPost(@RequestBody CreateGuestbookEntryRequest request, HttpServletResponse response) {
		try {
			guestbook.createEntry(request.getMessage());
		} catch (UserNotFoundException e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
		}
	}

	@RequestMapping(value = "/rest/guestbook/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deletePost(@PathVariable UUID id, HttpServletResponse response) throws UserNotFoundException {
		if (!guestbook.deleteEntry(id)) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}
}