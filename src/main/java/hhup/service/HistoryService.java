package hhup.service;

import hhup.model.history.HistoryItem;
import hhup.model.history.HistoryType;
import hhup.repository.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Service
public class HistoryService {

	@Autowired
	@Qualifier("HistoryRepository")
	private Repository<HistoryItem> history;

	public void recordEvent(UUID actorId, HistoryType type, String text, UUID... subjectIds) {
		history.add(new HistoryItem(type, actorId, Sets.newHashSet(subjectIds), text, DateTime.now()));
	}

	public List<HistoryItem> getHistoryFor(UUID actorOrSubjectId, int maxItems, boolean actorOnly) {

		List<HistoryItem> allEntriesSorted;
		if (actorOrSubjectId != null) {

			Predicate<HistoryItem> filter;

			if (actorOnly) {
				filter = item -> item.getActorId().equals(actorOrSubjectId);
			} else {
				filter = item -> {
					return (item.getActorId().equals(actorOrSubjectId) || item.getSubjectIds().contains(
							actorOrSubjectId));
				};
			}

			allEntriesSorted = sortByDate(history.findAll(filter));
		} else {
			allEntriesSorted = sortByDate(history);
		}

		if ((maxItems > -1) && (maxItems < allEntriesSorted.size())) { return allEntriesSorted.subList(0, maxItems); }
		return allEntriesSorted;
	}

	private List<HistoryItem> sortByDate(Iterable<HistoryItem> items) {
		ArrayList<HistoryItem> itemList = Lists.newArrayList(items);
		Collections.sort(itemList, (item1, item2) -> item1.getDate().compareTo(item2.getDate()));
		return itemList;
	}
}