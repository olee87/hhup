package hhup.service;

import hhup.model.exception.UserNotFoundException;
import hhup.model.history.HistoryType;
import hhup.model.user.InternalUserInfo;
import hhup.model.user.Note;
import hhup.repository.Repository;

import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Service
public class NoteService {

	@Autowired
	@Qualifier("NoteRepository")
	private Repository<Note> notes;

	@Autowired
	private UserService users;

	@Autowired
	private HistoryService history;

	public void addNote(UUID userId, String text) throws UserNotFoundException {
		UUID id = UUID.randomUUID();
		InternalUserInfo author = users.getCurrentUser();
		DateTime creationDate = DateTime.now();

		notes.add(new Note(text, author.getId(), id, userId, creationDate));

		InternalUserInfo user = users.getUserForId(userId);
		String message = author.getUsername() + " wrote a note on " + user.getUsername() + ": '" + text + "'";
		history.recordEvent(author.getId(), HistoryType.CREATED_NOTE, message, userId);
	}

	public void removeNote(UUID noteId) throws UserNotFoundException {
		Note note = notes.findFirst(n -> n.getId().equals(noteId));
		notes.remove(note);

		InternalUserInfo requestingUser = users.getCurrentUser();
		String message = deletedNoteMessage(note, requestingUser);
		history.recordEvent(requestingUser.getId(), HistoryType.DELETED_NOTE, message, noteId);
	}

	public Set<Note> getNotesForUser(UUID userId) {
	   return notes.findAll(note -> note.getUserId().equals(userId));
	}

	private String deletedNoteMessage(Note note, InternalUserInfo requestingUser) throws UserNotFoundException {
		String owner = requestingUser.getId().equals(note.getAuthorId()) ? "their"
				: users.getUserForId(note.getAuthorId()).getUsername() + "'s";
		String subject = users.getUserForId(note.getUserId()).getUsername();
		String message = requestingUser.getUsername() + " deleted " + owner + " note on "
				+ subject + ": " + note.getText();
		return message;
	}

	public Set<UUID> usersWithNotes() {
		return Sets.newHashSet(Iterables.<Note, UUID>transform(notes, note -> note.getUserId()));
	}
}