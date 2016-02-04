package hhup.web.controller;

import hhup.model.exception.UserNotFoundException;
import hhup.model.user.Note;
import hhup.service.NoteService;
import hhup.web.request.CreateNoteRequest;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest")
public class NoteController {

	@Autowired
	private NoteService notes;

	@RequestMapping(value = "/notes", method = RequestMethod.GET, params = {"userId"})
	@ResponseBody
	public Set<Note> getNotesFor(@RequestParam(value = "userId") UUID userId) {
		return notes.getNotesForUser(userId);
	}

	@RequestMapping(value = "/notes", method = RequestMethod.POST)
	@ResponseBody
	public void createNote(@RequestBody CreateNoteRequest request) throws UserNotFoundException {
		notes.addNote(request.getUserId(), request.getText());
	}

	@RequestMapping(value = "/notes", method = RequestMethod.DELETE, params = {"noteId"})
	@ResponseBody
	public void deleteNote(@RequestParam UUID noteId) throws UserNotFoundException {
		notes.removeNote(noteId);
	}

	@RequestMapping(value = "/notes/usersWithNotes", method = RequestMethod.GET)
	@ResponseBody
	public Set<UUID> usersWithNotes() {
		return notes.usersWithNotes();
	}
}