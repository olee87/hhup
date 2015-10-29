package hhup.service;

import hhup.model.exception.UserNotFoundException;
import hhup.model.guestbook.GuestbookEntry;
import hhup.model.history.HistoryType;
import hhup.model.user.InternalUserInfo;
import hhup.repository.Repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

@Service
public class GuestbookService {

	@Autowired
	@Qualifier("GuestbookRepository")
	private Repository<GuestbookEntry> repo;

	@Autowired
	private UserService users;

	@Autowired
	private HistoryService history;

	public List<GuestbookEntry> getAll() {
		return ImmutableList.copyOf(repo);
	}

	public boolean createEntry(String message) throws UserNotFoundException {
		InternalUserInfo user = users.getCurrentUser();
		boolean result = repo.add(new GuestbookEntry(user.getId(), message, DateTime.now()));

		String historyMessage = user.getUsername() + " wrote the guestbook entry '" + message + "'";
		history.recordEvent(user.getId(), HistoryType.WROTE_GUESTBOOK_ENTRY, historyMessage);
		return result;
	}

	public boolean deleteEntry(UUID id) throws UserNotFoundException {
		GuestbookEntry entry = repo.findFirst(x -> x.getId().equals(id));
		boolean result = repo.remove(entry);

		InternalUserInfo user = users.getCurrentUser();
		String message = user.getUsername() + " deleted the guestbook entry '" + entry.getMessage() + "'";
		history.recordEvent(user.getId(), HistoryType.DELETED_GUESTBOOK_ENTRY, message);
		return result;
	}
}